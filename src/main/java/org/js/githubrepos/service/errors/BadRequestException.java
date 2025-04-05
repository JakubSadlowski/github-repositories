package org.js.githubrepos.service.errors;

public class BadRequestException extends ServiceGeneralException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {super(message, cause);}
}
