package org.js.githubrepos.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import okhttp3.OkHttpClient;
import org.js.githubrepos.api.mappers.GithubReposResponseMapper;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.GithubReposResponse;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommonsLog
@Service
public class GithubReposService {
    private final ObjectMapper objectMapper;
    private final GithubHttpClient githubHttpClient;

    @Autowired
    public GithubReposService(OkHttpClient client, ObjectMapper objectMapper, GithubHttpClient githubHttpClient) {
        this.objectMapper = objectMapper;
        this.githubHttpClient = githubHttpClient;
    }

    public GithubReposResponse getUserRepositories(String username, String bearerToken) throws IOException {
        String responseBody = githubHttpClient.callGithubRepositories(username, bearerToken);

        List<Map<String, Object>> repositories = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {
        });

        List<RepositoryInfo> repositoryInfoList = repositories.stream()
            .filter(GithubReposResponseMapper::isNotFork)
            .map(repo -> {
                String repoName = (String) repo.get("name");
                String ownerLogin = (String) ((Map<String, Object>) repo.get("owner")).get("login");
                List<BranchInfo> branches = getBranchesForRepo(ownerLogin, repoName, bearerToken);

                return GithubReposResponseMapper.mapToRepositoryInfo(repo, branches);
            })
            .toList();

        return GithubReposResponseMapper.mapToGithubReposResponse(repositoryInfoList);
    }

    List<BranchInfo> getBranchesForRepo(String owner, String repoName, String bearerToken) {
        try {
            String responseBody = githubHttpClient.callGithubBranches(owner, repoName, bearerToken);

            List<Map<String, Object>> branches = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {
            });

            return GithubReposResponseMapper.mapToBranchInfoList(branches);
        } catch (IOException e) {
            log.error("Error fetching branches for repo " + owner + "/" + repoName, e);
            return new ArrayList<>();
        }
    }
}
