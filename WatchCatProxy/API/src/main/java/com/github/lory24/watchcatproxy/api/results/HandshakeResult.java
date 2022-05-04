package com.github.lory24.watchcatproxy.api.results;

import jdk.jfr.Unsigned;
import lombok.Getter;

/**
 * The Handshake result API class. This class will be instanced after the handshake sequence.
 *
 * @param protocolVersion The protocol version of the client. This can be used by the server for identify the client's Minecraft version.
 * @param serverAddress   The server address value passed in the handshake packet
 * @param serverPort      The port value passed in the handshake packet
 * @param nextState       The handshake next state value passed in the handshake packet. The passed object is a number, but in this API
 *                        class it's represented by an enum.
 */
public record HandshakeResult(@Getter int protocolVersion, @Getter String serverAddress,
                              @Getter @Unsigned int serverPort,
                              @Getter com.github.lory24.watchcatproxy.api.results.HandshakeResult.HandshakeNextState nextState) {

    /**
     * The handshake net state enum. This will be used when instancing the nextState object in the HandshakeResult
     * class.
     */
    public enum HandshakeNextState {

        /**
         * In the Minecraft protocol, this value is represented by the number 0x02
         */
        LOGIN(0x02),

        /**
         * This is a custom handshake state. It will be used to initialize a remote server management session. When a
         * connection with this state is created, all the packets will be sent in JSON format. The sequence is this:
         * LENGTH - DATA
         */
        WEBPANEL_ACTION(0x04),

        /**
         * In the Minecraft protocol, this value is represented by the number 0x01
         */
        STATUS(0x01);

        /**
         * The numerical value
         */
        @Getter
        private final int value;

        /**
         * The constructor for the enum.
         *
         * @param value The numeric value of the selected handshake state
         */
        HandshakeNextState(int value) {
            this.value = value;
        }

        /**
         * Convert a next state code (the number) to its enum value
         * @param i The number
         */
        public static HandshakeNextState convertIntegerToState(int i) {
            return i == 1 ? HandshakeResult.HandshakeNextState.STATUS : HandshakeResult.HandshakeNextState.LOGIN;
        }
    }

}
