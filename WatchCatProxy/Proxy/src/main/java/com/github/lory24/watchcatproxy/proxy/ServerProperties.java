package com.github.lory24.watchcatproxy.proxy;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Objects;

@SuppressWarnings("unused")
enum ServerProperties {
    // Version section
    serverVersion("serverVersion"),
    protocolVersion("protocolVersion"),

    // Players section
    maxPlayers("maxPlayers"),
    enableCustomSample("enableCustomSample"),
    sample("sample"),

    // Players -> fakeOnline section
    fakeOnlineEnabled("enabled"),
    fakeOnlineValue("value"),

    // Server general settings
    port("port"),
    serverName("serverName"),
    serverMessageOfTheDay("serverMessageOfTheDay"),
    serverIconName("serverIconName"),
    serverEnableExploitTotalCooldown("enableExploitTotalCooldown"),
    ;

    @SuppressWarnings("resource")
    @NotNull
    public static String loadFileContent(@NotNull File serverProperties) throws IOException, URISyntaxException {
        FileInputStream fileInputStream = new FileInputStream(serverProperties);
        return new String(fileInputStream.readAllBytes());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void loadServerPropertiesFile(@NotNull File serverProperties, WatchCatProxy proxy)
            throws IOException {
        if (!serverProperties.exists()) {
            serverProperties.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(serverProperties);
            InputStream inputStream = proxy.getClass().getClassLoader().getResourceAsStream("server-properties.json");
            fileOutputStream.write(Objects.requireNonNull(inputStream).readAllBytes());
            fileOutputStream.flush(); fileOutputStream.close();
        }
    }

    private final String key;
    ServerProperties(String key) {
        this.key = key;
    }

    public Object get(@NotNull JSONObject jsonObject) {
        return jsonObject.get(this.key);
    }
}
