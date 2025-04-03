package org.js.githubrepos.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GithubReposService {
    private static final String GITHUB_API_URL_BASE = "https://api.github.com";

    private final OkHttpClient okHttpClient;

    @Value("${github.token:${GITHUB_TOKEN:}}")
    private String githubToken;

    @Autowired
    public GithubReposService(OkHttpClient client) {
        this.okHttpClient = client;
    }

    String executeListRepositoriesForUserEndpoint(String url, String repoOwnerLogin) throws IOException {
        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

        Request.Builder requestBuilder = new Request.Builder()
            .url(baseUrl + "/users/" + repoOwnerLogin + "/repos")
            .header("Accept", "application/vnd.github.v3+json");

        if (githubToken != null && !githubToken.trim().isEmpty()) {
            requestBuilder.header("Authorization", "token " + githubToken);
        } else {
            System.out.println("WARNING: GitHub token is not configured. Requests are limited to the rate limit of the Github API.");
        }

        Request request = requestBuilder.build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    //throw new UserNotFoundException("User not found: " + url.split("/users/")[1].split("/")[0]);
                }

                if (response.code() == 403 && response.header("X-RateLimit-Remaining") != null
                    && Integer.parseInt(response.header("X-RateLimit-Remaining")) == 0) {
                    /*throw new RateLimitExceededException("GitHub API rate limit exceeded. Reset at: "
                        + response.header("X-RateLimit-Reset"));*/
                }
                throw new IOException("Unexpected response code: " + response);
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            return response.body().string();
        }
    }
}
