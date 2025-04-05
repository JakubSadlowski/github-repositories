package org.js.githubrepos.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.js.githubrepos.api.validation.BadRequestException;
import org.js.githubrepos.api.validation.ServiceGeneralException;
import org.js.githubrepos.config.GithubConfig;
import org.js.githubrepos.service.exceptions.GithubLoginNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GithubHttpClient {
    private final GithubConfig githubConfig;
    private final OkHttpClient okHttpClient;

    public GithubHttpClient(GithubConfig githubConfig, OkHttpClient okHttpClient) {
        this.githubConfig = githubConfig;
        this.okHttpClient = okHttpClient;
    }

    public String callGithubRepositories(String githubLogin, String bearerToken) throws IOException {
        String url = githubConfig.getUrlOfGithubServer() + "/users/" + githubLogin + "/repos";

        StringResponse response = callUrlUsingOkHttp(url, bearerToken);
        if (HttpStatus.NOT_FOUND.equals(response.getStatus())) {
            throw new GithubLoginNotFoundException("Github login for given user not found: " + githubLogin);
        }

        return response.getBody();
    }

    String callGithubBranches(String username, String repoName, String bearerToken) throws IOException {
        String url = githubConfig.getUrlOfGithubServer() + "/repos/" + username + "/" + repoName + "/branches";

        StringResponse response = callUrlUsingOkHttp(url, bearerToken);
        if (!HttpStatus.OK.equals(response.getStatus())) {
            throw new ServiceGeneralException(String.format("Failed to request url %s", url));
        }

        return response.getBody();
    }

    private StringResponse callUrlUsingOkHttp(String url, String authorizationToken) {
        Request request = createRequest(url, authorizationToken);

        try (Response response = okHttpClient.newCall(request)
            .execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return new StringResponse(HttpStatus.NOT_FOUND, null);
                } else if (response.code() == 403 && response.header("X-RateLimit-Remaining") != null
                    && Integer.parseInt(response.header("X-RateLimit-Remaining")) == 0) {
                    throw new ServiceGeneralException("GitHub API rate limit exceeded. Reset at: " + response.header("X" + "-RateLimit-Reset"));
                }
                throw new ServiceGeneralException("Unexpected response code: " + response);
            } else if (response.body() == null) {
                throw new BadRequestException(String.format("Failed to request url %s. Response body is not provided", url));
            }
            return new StringResponse(HttpStatus.OK,
                response.body()
                    .string());
        } catch (IOException e) {
            throw new ServiceGeneralException(String.format("Failed to request url %s", url), e);
        }
    }

    private static Request createRequest(String url, String authorizationToken) {
        Request.Builder requestBuilder = new Request.Builder().url(url)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28");

        if (authorizationToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authorizationToken);
        }

        return requestBuilder.build();
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class StringResponse {
        private final HttpStatus status;
        private final String body;
    }

}
