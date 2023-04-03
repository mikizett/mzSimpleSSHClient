package com.mz.sshclient.ssh.sftp;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum TransferMode {

    @JsonEnumDefaultValue
    NORMAL(0, "Transfer normally"),
    BACKGROUND(1, "Transfer in background")
    ;

    private final int key;
    private String value;

    TransferMode(int pKey, String pValue) {
        this.key = pKey;
        this.value = pValue;
    }

    public int getKey() {
        return key;
    }

    @Override
    public String toString() {
        return value;
    }

}
