package com.mz.sshclient.ssh.exceptions;

public class SshOperationCanceledException extends Exception {

    public SshOperationCanceledException(final String message) {
        super(message);
    }

    public SshOperationCanceledException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
