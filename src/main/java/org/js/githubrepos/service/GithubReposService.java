package org.js.githubrepos.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.coyote.BadRequestException;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.js.githubrepos.service.exceptions.GithubLoginNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommonsLog
@Service
public class GithubReposService {
    private static final String GITHUB_API_URL_BASE = "https://api.github.com";

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public GithubReposService(OkHttpClient client, ObjectMapper objectMapper) {
        this.okHttpClient = client;
        this.objectMapper = objectMapper;
    }

    public List<RepositoryInfo> getUserRepositories(String username) throws IOException {
        String responseBody = executeListRepositoriesForUserEndpoint(username);

        List<Map<String, Object>> repositories = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {
        });

        return repositories.stream()
            .filter(repo -> !(boolean) repo.get("fork"))
            .map(repo -> {
                String repoName = (String) repo.get("name");
                String ownerLogin = (String) ((Map<String, Object>) repo.get("owner")).get("login");
                List<BranchInfo> branches = getBranchesForRepo(ownerLogin, repoName);
                return RepositoryInfo.builder()
                    .repositoryName(repoName)
                    .ownerLogin(ownerLogin)
                    .branches(branches)
                    .build();
            })
            .collect(Collectors.toList());
    }

    List<BranchInfo> getBranchesForRepo(String owner, String repoName) {
        try {
            String responseBody = executeListBranchesForUserRepositoryEndpoint(owner, repoName);

            List<Map<String, Object>> branches = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {
            });

            return branches.stream()
                .map(branch -> {
                    String branchName = (String) branch.get("name");
                    String commitSha = (String) ((Map<String, Object>) branch.get("commit")).get("sha");
                    return BranchInfo.builder()
                        .branchName(branchName)
                        .lastCommitSHA(commitSha)
                        .build();
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error fetching branches for repo " + owner + "/" + repoName, e);
            return new ArrayList<>();
        }
    }

    String executeListRepositoriesForUserEndpoint(String username) throws IOException {

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

    String executeListBranchesForUserRepositoryEndpoint(String username, String repoName) throws IOException {
        String branchesUrl = GITHUB_API_URL_BASE + "/repos/" + username + "/" + repoName + "/branches";

        Request.Builder requestBuilder = new Request.Builder().url(branchesUrl)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28");

        Request request = requestBuilder.build();

        try (Response response = okHttpClient.newCall(request)
            .execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            if (response.body() == null) {
                throw new BadRequestException("Provided input is empty.");
            }

            return response.body()
                .string();
        }
    }
}
