package com.mz.sshclient.ssh.exceptions;

public class SshConnectionException extends Exception {

    public SshConnectionException(final String message) {
        super(message);
    }

    public SshConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
