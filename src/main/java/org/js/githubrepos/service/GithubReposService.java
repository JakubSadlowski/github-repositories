package org.js.githubrepos.service;

import lombok.extern.apachecommons.CommonsLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.coyote.BadRequestException;
import org.js.githubrepos.service.exceptions.GithubLoginNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@CommonsLog
@Service
public class GithubReposService {
    private static final String GITHUB_API_URL_BASE = "https://api.github.com";

    private final OkHttpClient okHttpClient;

    @Autowired
    public GithubReposService(OkHttpClient client) {
        this.okHttpClient = client;
    }

    public String executeListRepositoriesForUserEndpoint(String username) throws IOException {

        Request.Builder requestBuilder = new Request.Builder().url(GITHUB_API_URL_BASE + "/users/" + username + "/repos")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28");

        Request request = requestBuilder.build();

        try (Response response = okHttpClient.newCall(request)
            .execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    throw new GithubLoginNotFoundException("Github login for given user not found: " + username);
                }

                if (response.code() == 403 && response.header("X-RateLimit-Remaining") != null && Integer.parseInt(response.header("X-RateLimit-Remaining")) == 0) {
                    throw new RuntimeException("GitHub API rate limit exceeded. Reset at: " + response.header("X" + "-RateLimit-Reset"));
                }

                throw new IOException("Unexpected response code: " + response);
            }

            if (response.body() == null) {
                throw new BadRequestException("Provided input is empty.");
            }

            return response.body()
                .string();
        }
    }
}
