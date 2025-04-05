package org.js.githubrepos.service.json_mappers;

import org.js.githubrepos.api.model.RepositoryInfo;

import java.util.Map;

public class RepositoryInfoMapper {

    public static RepositoryInfo mapToRepositoryInfo(Map<String, Object> rawRepository) {
        String repoName = (String) rawRepository.get("name");
        @SuppressWarnings("unchecked") Map<String, Object> ownerMap = (Map<String, Object>) rawRepository.get("owner");
        String ownerLogin = (String) ownerMap.get("login");

        return RepositoryInfo.builder()
            .repositoryName(repoName)
            .ownerLogin(ownerLogin)
            .build();
    }

}
