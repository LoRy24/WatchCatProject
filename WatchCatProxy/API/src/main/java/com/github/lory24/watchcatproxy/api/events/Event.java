package com.github.lory24.watchcatproxy.api.events;

import lombok.Getter;
import lombok.Setter;

public abstract class Event {

    /**
     * This value represents if the event is cancelled
     */
    @Getter @Setter
    private boolean cancelled;

    /**
     * All the default actions that will be executed then the event is fired, and it's not cancelled.
     */
    public abstract void onEvent();
}
