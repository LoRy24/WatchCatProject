package com.github.lory24.watchcatproxy.api;

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
}
