package com.mz.sshclient.ssh.exceptions;

public class SshDisconnectException extends Exception {

    public SshDisconnectException(final String message) {
        super(message);
    }

    public SshDisconnectException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
