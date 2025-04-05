package org.js.githubrepos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.jetbrains.annotations.NotNull;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.GithubRepositoryResponse;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.js.githubrepos.service.errors.ServiceGeneralException;
import org.js.githubrepos.service.json_mappers.RepositoryBranchMapper;
import org.js.githubrepos.service.json_mappers.RepositoryInfoMapper;
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
    public GithubReposService(ObjectMapper objectMapper, GithubHttpClient githubHttpClient) {
        this.objectMapper = objectMapper;
        this.githubHttpClient = githubHttpClient;
    }

    public GithubRepositoryResponse getUserRepositories(String username, String bearerToken) throws IOException {
        String githubRepositoriesJson = githubHttpClient.getGithubRepositories(username, bearerToken);

        List<RepositoryInfo> repositoriesInfoList = mapToRepositoriesInfoList(bearerToken, githubRepositoriesJson);

        return GithubRepositoryResponse.builder()
            .repositoryList(repositoriesInfoList)
            .build();
    }

    @NotNull
    private List<RepositoryInfo> mapToRepositoriesInfoList(String bearerToken, String githubRepositoriesJson) {
        List<Map<String, Object>> repositoriesRaw = mapJsonToMap(githubRepositoriesJson);

        List<RepositoryInfo> repositoriesInfoList = new ArrayList<>();
        for (Map<String, Object> repositoryMap : repositoriesRaw) {
            if (isFork(repositoryMap))
                continue;
            RepositoryInfo repository = RepositoryInfoMapper.mapToRepositoryInfo(repositoryMap);
            List<BranchInfo> branches = getRepositoryBranches(repository.getOwnerLogin(), repository.getRepositoryName(), bearerToken);
            repository.getBranches()
                .addAll(branches);
            repositoriesInfoList.add(repository);
        }
        return repositoriesInfoList;
    }

    public static boolean isFork(Map<String, Object> repo) {
        Object forkObj = repo.get("fork");
        return ((boolean) forkObj);
    }

    List<BranchInfo> getRepositoryBranches(String owner, String repoName, String bearerToken) {
        String responseBody = githubHttpClient.callGithubBranches(owner, repoName, bearerToken);
        List<Map<String, Object>> branches = mapJsonToMap(responseBody);
        return RepositoryBranchMapper.mapToBranchesList(branches);
    }

    private List<Map<String, Object>> mapJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ServiceGeneralException(String.format("Failed to map JSON [%s]", json), e);
        }
    }
}
