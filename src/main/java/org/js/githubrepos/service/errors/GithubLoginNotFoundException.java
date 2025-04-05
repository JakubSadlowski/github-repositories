package org.js.githubrepos.service.errors;

public class GithubLoginNotFoundException extends ServiceGeneralException {
    public GithubLoginNotFoundException(String message) {
        super(message);
    }

    public GithubLoginNotFoundException(String message, Throwable cause) {super(message, cause);}
}
