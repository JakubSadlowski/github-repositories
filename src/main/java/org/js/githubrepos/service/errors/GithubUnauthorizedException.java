package org.js.githubrepos.service.errors;

public class GithubUnauthorizedException extends ServiceGeneralException {
    public GithubUnauthorizedException(String message) {
        super(message);
    }

    public GithubUnauthorizedException(String message, Throwable cause) {super(message, cause);}
}
