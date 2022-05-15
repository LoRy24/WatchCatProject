package com.github.lory24.watchcatproxy.api.events.data;

/**
 * The preLogin data used by a preLoginEvent listener.
 * @param enableOnlineMode If the InitialHandler is going to enable the premium (online) mode.
 */
public record PreLoginData(boolean enableOnlineMode) {
}
