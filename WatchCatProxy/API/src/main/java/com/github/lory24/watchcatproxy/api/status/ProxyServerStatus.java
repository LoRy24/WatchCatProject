package com.github.lory24.watchcatproxy.api.status;

import com.github.lory24.watchcatproxy.api.ProxiedPlayer;
import com.github.lory24.watchcatproxy.api.chatcomponent.TextChatComponent;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

/**
 * @param version     The version field
 * @param players     The players field
 * @param description The description ChatComponent field
 * @param favIcon     The favicon field
 */
public record ProxyServerStatus(@Getter @Setter ProxyServerStatus.StatusVersion version, @Getter @Setter ProxyServerStatus.StatusPlayers players, @Getter @Setter TextChatComponent description,
                                @Getter @Setter ProxyServerStatus.FavIcon favIcon) {

    /**
     * The status version record object. This is used to obtain the "version" object of the status json response
     *
     * @param name     The version name
     * @param protocol The protocol version
     */
    public record StatusVersion(@Getter String name, @Getter int protocol) {

        /**
         * Get a json object that will be used in the ProxyServerStatus class
         *
         * @return The Json object
         */
        @NotNull
        @Contract(pure = true)
        public String getJson() {
            return "{\"name\": \"" + name + "\", \"protocol\": " + protocol + "}";
        }
    }

    /**
     * The sample player object. This is the datatype of the sample array.
     *
     * @param name The name of the player
     * @param id   The id of the player
     */
    public record SampleStatusPlayer(@Getter String name, @Getter String id) {

        /**
         * Get a json object that will be used in the ProxyServerStatus class
         *
         * @return The Json object
         */
        @NotNull
        @Contract(pure = true)
        public String getJson() {
            return "{\"name\": \"" + name + "\", \"id\": " + id + "}";
        }
    }

    public record StatusPlayers(@Getter int max, @Getter int online, SampleStatusPlayer[] sample) {

        /**
         * Get a json object that will be used in the ProxyServerStatus class
         *
         * @return The Json object
         */
        @NotNull
        @Contract(pure = true)
        public String getJson() {
            JSONArray sampleArray = new JSONArray();
            for (SampleStatusPlayer samplePlayer : sample) sampleArray.put(new JSONObject(samplePlayer.getJson()));
            return "{\"max\": " + max + ", \"online\": " + online + (sample.length > 0 ? ", \"sample\": " + sampleArray + "}" : "}");
        }

        /**
         * Build an array of samples from a string. Every sample entry is divided by the new line escape character (\n)
         */
        @NotNull
        public static SampleStatusPlayer[] buildSampleFromString(@NotNull String s) {
            String[] lines = s.split("\n");
            SampleStatusPlayer[] result = new SampleStatusPlayer[lines.length];
            for (int i = 0; i < lines.length; i++)
                result[i] = new SampleStatusPlayer(lines[i], UUID.randomUUID().toString());
            return result;
        }

        /**
         * Build an array of samples from a ProxiedPlayers hashmap.
         */
        @NotNull
        public static SampleStatusPlayer[] buildSampleFromPlayersHashMap(
                @NotNull HashMap<String, ProxiedPlayer> players) {
            SampleStatusPlayer[] result = new SampleStatusPlayer[players.size()];
            int i = 0;
            for (ProxiedPlayer p: players.values()) result[i++] = new SampleStatusPlayer(p.getUsername(), p.getUUID().toString());
            return result;
        }
    }

    /**
     * @param file The icon .png file
     */
    public record FavIcon(File file) {

        /**
         * Get the base64 encoded icon
         */
        public String getBase64() throws IOException {
            byte[] imageBytes = Files.readAllBytes(Path.of(file.toURI()));
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    /**
     * Build the ProxyServerStatus to a JSON object
     * @return the json
     */
    @NotNull
    public String buildJSON() throws IOException {
        JSONObject result = new JSONObject();
        result.put("version", new JSONObject((this.version.getJson()))); result.put("players", new JSONObject(this.players.getJson()));
        result.put("description", new JSONObject(this.description.buildTextChatComponent()));
        if (this.favIcon != null && this.favIcon.file.exists()) result.put("favicon", "data:image/png;base64," +
                this.favIcon.getBase64());
        return result.toString().replace("§", "\\u00a7");
    }
}
