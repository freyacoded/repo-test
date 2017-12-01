/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.client.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class LavalinkLoadBalancer {

    private Lavalink lavalink;
    //private Map<String, Optional<LavalinkSocket>> socketMap = new ConcurrentHashMap<>();
    private List<PenaltyProvider> penaltyProviders = new ArrayList<>();

    LavalinkLoadBalancer(Lavalink lavalink) {
        this.lavalink = lavalink;
    }

    public LavalinkSocket determineBestSocket(long guild) {
        LavalinkSocket leastPenalty = null;
        int record = Integer.MAX_VALUE;

        for (LavalinkSocket socket : lavalink.getNodes()) {
            int total = getPenalties(socket, guild, penaltyProviders).getTotal();
            if (total < record) {
                leastPenalty = socket;
                record = total;
            }
        }

        if (leastPenalty == null)
            throw new IllegalStateException("No available nodes!");

        if (!leastPenalty.isOpen()) return null;

        return leastPenalty;
    }

    public void addPenalty(PenaltyProvider penalty) {
        this.penaltyProviders.add(penalty);
    }

    public void removePenalty(PenaltyProvider penalty) {
        this.penaltyProviders.remove(penalty);
    }

    void onNodeDisconnect(LavalinkSocket disconnected) {
        lavalink.getLinks().forEach(link -> {
            if (disconnected.equals(link.getCurrentSocket()))
                link.onNodeDisconnected();
        });
    }

    void onNodeConnect(LavalinkSocket connected) {
        lavalink.getLinks().forEach(link -> {
            if (link.getCurrentSocket() == null)
                link.changeNode(connected);
        });
    }

    public Penalties getPenalties(LavalinkSocket socket, long guild, List<PenaltyProvider> penaltyProviders) {
        return new Penalties(socket, guild, penaltyProviders);
    }

    public static Penalties getPenalties(LavalinkSocket socket) {
        return new Penalties(socket, 0L, Collections.emptyList());
    }

    @SuppressWarnings("unused")
    public static class Penalties {

        private LavalinkSocket socket;
        private final long guild;
        private int playerPenalty = 0;
        private int cpuPenalty = 0;
        private int deficitFramePenalty = 0;
        private int nullFramePenalty = 0;
        private int customPenalties = 0;

        private Penalties(LavalinkSocket socket, long guild, List<PenaltyProvider> penaltyProviders) {
            this.socket = socket;
            this.guild = guild;
            if (socket.stats == null) return; // Will return as max penalty anyways
            // This will serve as a rule of thumb. 1 playing player = 1 penalty point
            playerPenalty = socket.stats.getPlayingPlayers();

            // https://fred.moe/293.png
            cpuPenalty = (int) Math.pow(1.05d, 100 * socket.stats.getSystemLoad()) * 10 - 10;

            // -1 Means we don't have any frame stats. This is normal for very young nodes
            if (socket.stats.getAvgFramesDeficitPerMinute() != -1) {
                // https://fred.moe/rjD.png
                deficitFramePenalty = (int) (Math.pow(1.03d, 500f * ((float) socket.stats.getAvgFramesDeficitPerMinute() / 3000f)) * 600 - 600);
                nullFramePenalty = (int) (Math.pow(1.03d, 500f * ((float) socket.stats.getAvgFramesNulledPerMinute() / 3000f)) * 300 - 300);
                nullFramePenalty *= 2;
                // Deficit frames are better than null frames, as deficit frames can be caused by the garbage collector
            }
            penaltyProviders.forEach(pp -> customPenalties += pp.getPenalty(this));
        }

        public LavalinkSocket getSocket() {
            return socket;
        }

        public long getGuild() {
            return guild;
        }

        public int getPlayerPenalty() {
            return playerPenalty;
        }

        public int getCpuPenalty() {
            return cpuPenalty;
        }

        public int getDeficitFramePenalty() {
            return deficitFramePenalty;
        }

        public int getNullFramePenalty() {
            return nullFramePenalty;
        }

        public int getCustomPenalties() {
            return this.customPenalties;
        }

        public int getTotal() {
            if (socket.stats == null) return (Integer.MAX_VALUE - 1);
            return playerPenalty + cpuPenalty + deficitFramePenalty + nullFramePenalty + customPenalties;
        }

        @Override
        public String toString() {
            if (!socket.isAvailable()) return "Penalties{" +
                    "unavailable=" + (Integer.MAX_VALUE - 1) +
                    '}';

            return "Penalties{" +
                    "total=" + getTotal() +
                    ", playerPenalty=" + playerPenalty +
                    ", cpuPenalty=" + cpuPenalty +
                    ", deficitFramePenalty=" + deficitFramePenalty +
                    ", nullFramePenalty=" + nullFramePenalty +
                    ", custom=" + customPenalties +
                    '}';
        }
    }

}
