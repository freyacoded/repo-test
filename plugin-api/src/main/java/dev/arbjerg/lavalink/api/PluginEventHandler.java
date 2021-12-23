package dev.arbjerg.lavalink.api;

/**
 * Must be provided as a bean
 */
public abstract class PluginEventHandler {

    /**
     * Fired upon a new WebSocket being opened
     * @param context the websocket
     * @param resumed if the context was resumed and thus reused
     */
    void onWebSocketOpen(ISocketContext context, boolean resumed) {}

    /**
     * Fired upon a WebSocket being closed
     * @param context the websocket
     */
    void onWebSocketClose(ISocketContext context) {}

    /**
     * Fired upon a WebSocket message being received
     * @param context the websocket
     * @param message the message, presumably in JSON
     */
    void onWebsocketMessageIn(ISocketContext context, String message) {}

    /**
     * Fired upon a WebSocket message being sent
     * @param context the websocket
     * @param message the message, presumably in JSON
     */
    void onWebSocketMessageOut(ISocketContext context, String message) {}

    /**
     * Fired upon a new player being created
     * @param context the websocket
     * @param player the new player
     */
    void onNewPlayer(ISocketContext context, IPlayer player) {}

    /**
     * Fired upon a player being destroyed
     * @param context the websocket
     * @param player the player to be destroyed
     */
    void onDestroyPlayer(ISocketContext context, IPlayer player) {}
}
