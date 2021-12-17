package dev.arbjerg.lavalink.api;

import org.json.JSONObject;

import java.util.Map;

/**
 * Represents a WebSocket connection
 */
public interface ISocketContext {
    /**
     * Returns the player of a guild. Never returns null.
     * @param guildId the guild the player is associated with
     * @return a potentially newly-created player
     */
    IPlayer getPlayer(long guildId);

    /**
     * @return a read-only map of all players associated by their guild
     */
    Map<Long, IPlayer> getPlayers();

    /**
     * @param guildId guild for which to remove player state from
     */
    void destroyPlayer(long guildId);

    /**
     * @param message a JSON message to send to the WebSocket client
     */
    void sendMessage(JSONObject message);

    /**
     * Closes this WebSocket
     */
    void closeWebSocket();

    /**
     * @param closeCode the close code to send to the client
     */
    void closeWebSocket(int closeCode);

    /**
     *
     * @param closeCode the close code to send to the client
     * @param reason the close reason to send to the client
     */
    void closeWebSocket(int closeCode, String reason);
}
