package org.js.githubrepos.api.validation;

public class BadRequestException extends ServiceGeneralException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {super(message, cause);}
}
