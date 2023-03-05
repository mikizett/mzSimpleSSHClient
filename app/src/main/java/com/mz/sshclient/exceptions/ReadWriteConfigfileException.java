package com.mz.sshclient.exceptions;

public class ReadWriteConfigfileException extends Exception {
    public ReadWriteConfigfileException(final String message) {
        super(message);
    }

    public ReadWriteConfigfileException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
