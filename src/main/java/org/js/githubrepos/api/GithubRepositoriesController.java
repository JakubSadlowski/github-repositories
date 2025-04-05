package org.js.githubrepos.api;

import lombok.extern.apachecommons.CommonsLog;
import org.js.githubrepos.api.model.Error;
import org.js.githubrepos.api.model.GithubReposResponse;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.js.githubrepos.api.validation.BadRequestException;
import org.js.githubrepos.service.GithubReposService;
import org.js.githubrepos.service.exceptions.GithubLoginNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.List;

@CommonsLog
@RestController
@RequestMapping("/v1/")
public class GithubRepositoriesController {
    private final GithubReposService githubReposService;

    @Autowired
    public GithubRepositoriesController(GithubReposService githubReposService) {
        this.githubReposService = githubReposService;
    }

    @GetMapping("github-repos/{githubLogin}")
    public ResponseEntity<GithubReposResponse> getGithubRepositoriesInfo(@PathVariable("githubLogin") String githubLogin,
        @RequestHeader(name = "Authorization", required = false) String bearerToken) throws IOException {
        return ResponseEntity.ok(githubReposService.getUserRepositories(githubLogin, bearerToken));
    }

    @ExceptionHandler(GithubLoginNotFoundException.class)
    public ResponseEntity<Error> handleGithubLoginNotFoundException(GithubLoginNotFoundException ex, WebRequest request) {
        Error response = Error.of("NOT_FOUND", ex.getMessage());
        log.warn("Handled GithubLoginNotFoundException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Error> handleBadRequestException(Exception ex, WebRequest request) {
        Error response = Error.of("INVALID_INPUT", ex.getMessage());
        log.warn("Handled BadRequestException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }
}
