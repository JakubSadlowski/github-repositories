package org.js.githubrepos.service.exceptions;

import org.js.githubrepos.api.validation.ServiceGeneralException;

public class GithubUnauthorizedException extends ServiceGeneralException {
    public GithubUnauthorizedException(String message) {
        super(message);
    }

    public GithubUnauthorizedException(String message, Throwable cause) {super(message, cause);}
}
