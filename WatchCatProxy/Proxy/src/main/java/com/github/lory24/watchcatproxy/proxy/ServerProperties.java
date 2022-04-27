package com.github.lory24.watchcatproxy.proxy;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
public enum ServerProperties {
    // Version section
    serverVersion("serverVersion"),
    protocolVersion("protocolVersion"),

    // Players section
    maxPlayers("maxPlayers"),

    // Players -> fakeOnline section
    fakeOnlineEnabled("enabled"),
    fakeOnlineValue("value"),

    // Server general settings
    port("port"),
    serverName("serverName"),
    serverMessageOfTheDay("serverMessageOfTheDay"),
    serverIconName("serverIconName"),
    ;

    @SuppressWarnings("resource")
    @NotNull
    public static String loadFileContent(@NotNull File serverProperties) throws IOException, URISyntaxException {
        FileInputStream fileInputStream = new FileInputStream(serverProperties);
        return new String(fileInputStream.readAllBytes());
    }

    private final String key;
    ServerProperties(String key) {
        this.key = key;
    }

    public Object get(@NotNull JSONObject jsonObject) {
        return jsonObject.get(this.key);
    }
}
