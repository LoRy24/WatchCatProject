package com.github.lory24.watchcatproxy.api.status;

import com.github.lory24.watchcatproxy.api.ChatComponent;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * @param version     The version field
 * @param players     The players field
 * @param description The description ChatComponent field
 * @param favIcon     The favicon field
 */
public record ProxyServerStatus(@Getter ProxyServerStatus.StatusVersion version, @Getter ProxyServerStatus.StatusPlayers players,
        @Getter ChatComponent description, @Getter ProxyServerStatus.FavIcon favIcon) {

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
        public JSONObject getJson() {
            return new JSONObject("{\"name\": \"" + name + "\", \"protocol\": " + protocol + "}");
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
        public JSONObject getJson() {
            return new JSONObject("{\"name\": \"" + name + "\", \"id\": " + id + "}");
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
        public JSONObject getJson() {
            JSONArray sampleArray = new JSONArray();
            for (SampleStatusPlayer samplePlayer : sample) sampleArray.put(samplePlayer.getJson());
            return new JSONObject("{\"max\": " + max + ", \"online\": " + online + ", \"sample\": " + sampleArray + "}");
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
}
