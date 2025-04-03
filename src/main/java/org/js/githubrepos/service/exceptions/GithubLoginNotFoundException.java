package org.js.githubrepos.service.exceptions;

import org.js.githubrepos.api.validation.ServiceGeneralException;

public class GithubLoginNotFoundException extends ServiceGeneralException {
    public GithubLoginNotFoundException(String message) {
        super(message);
    }

    public GithubLoginNotFoundException(String message, Throwable cause) {super(message, cause);}
}
