package org.js.githubrepos.api.validation;

public class ServiceGeneralException extends RuntimeException {
    public ServiceGeneralException(String message) {
        super(message);
    }

    public ServiceGeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
