package com.mz.sshclient.services.exceptions;

public class ServiceRegistrationException extends Exception {
    public ServiceRegistrationException(String message) {
        super(message);
    }

    public ServiceRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
