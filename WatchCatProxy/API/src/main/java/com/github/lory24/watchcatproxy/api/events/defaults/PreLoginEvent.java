package com.github.lory24.watchcatproxy.api.events.defaults;

import com.github.lory24.watchcatproxy.api.events.Event;
import com.github.lory24.watchcatproxy.api.events.data.PreLoginData;
import lombok.Getter;

public class PreLoginEvent extends Event {

    /**
     * The PreLoginData, used by the listener to set and get some important values during the login state
     */
    @Getter
    private final PreLoginData preLoginData;

    /**
     * The constructor for the PreLoginEvent class.
     * @param preLoginData The preLoginData.
     */
    public PreLoginEvent(PreLoginData preLoginData) {
        this.preLoginData = preLoginData;
    }
}
