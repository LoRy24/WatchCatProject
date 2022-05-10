package com.github.lory24.watchcatproxy.protocol;

import javax.crypto.SecretKey;

public record EncryptionUtil(SecretKey aesSecret) {

    public byte encryptByte(byte b) {
        return b;
    }

    public byte decryptByte(byte b) {
        return b;
    }
}
