package com.github.lory24.watchcatproxy.proxy.timeout;

import lombok.Getter;

public enum TimeoutLevel {
    MINIMUM(5),
    MEDIUM(20),
    MAXIMUM(40),
    ;

    @Getter
    private final int ticks;

    TimeoutLevel(int ticks) {this.ticks = ticks;}
}
