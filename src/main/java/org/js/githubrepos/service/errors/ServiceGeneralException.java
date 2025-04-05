package org.js.githubrepos.service.errors;

public class ServiceGeneralException extends RuntimeException {
    public ServiceGeneralException(String message) {
        super(message);
    }

    public ServiceGeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
