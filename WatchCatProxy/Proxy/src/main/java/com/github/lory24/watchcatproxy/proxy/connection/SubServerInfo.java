package com.github.lory24.watchcatproxy.proxy.connection;

import lombok.Getter;

public record SubServerInfo(@Getter String name, @Getter String host, @Getter int port) {
}
