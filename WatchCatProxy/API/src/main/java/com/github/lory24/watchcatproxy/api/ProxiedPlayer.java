package com.github.lory24.watchcatproxy.api;

import java.io.IOException;
import java.util.UUID;

public abstract class ProxiedPlayer {

    /**
     * This function will return the name of the player
     */
    public abstract String getUsername();

    /**
     * This function will return the given userID.
     */
    public abstract UUID getUUID();

    /**
     * This function will return if the player is in online mode or not
     */
    public abstract boolean isInOnlineMode();

    /**
     * This function will disconnect a player from the server with a reason
     */
    public abstract void disconnect(String reason) throws IOException;
}
