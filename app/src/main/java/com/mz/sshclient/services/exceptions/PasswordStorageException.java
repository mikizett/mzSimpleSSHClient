package com.mz.sshclient.services.exceptions;

public class PasswordStorageException extends Exception {
    public PasswordStorageException(String message) {
        super(message);
    }

    public PasswordStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
