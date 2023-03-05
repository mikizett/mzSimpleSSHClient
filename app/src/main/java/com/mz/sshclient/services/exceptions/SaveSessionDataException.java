package com.mz.sshclient.services.exceptions;

public class SaveSessionDataException extends Exception {
    public SaveSessionDataException(String message) {
        super(message);
    }

    public SaveSessionDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

