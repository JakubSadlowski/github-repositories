package org.js.githubrepos.api.mappers;

import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.GithubRepositoryResponse;
import org.js.githubrepos.api.model.RepositoryInfo;

import java.util.List;
import java.util.Map;

public class GithubReposResponseMapper {
    private GithubReposResponseMapper() {
    }

    public static GithubRepositoryResponse mapToGithubReposResponse(List<RepositoryInfo> repositoryInfos) {
        return GithubRepositoryResponse.builder()
            .repositoryList(repositoryInfos)
            .build();
    }

    public static RepositoryInfo mapToRepositoryInfo(Map<String, Object> repo, List<BranchInfo> branches) {
        String repoName = (String) repo.get("name");
        String ownerLogin = (String) ((Map<String, Object>) repo.get("owner")).get("login");

        return RepositoryInfo.builder()
            .repositoryName(repoName)
            .ownerLogin(ownerLogin)
            .branches(branches)
            .build();
    }

    public static List<BranchInfo> mapToBranchInfoList(List<Map<String, Object>> branches) {
        return branches.stream()
            .map(GithubReposResponseMapper::mapToBranchInfo)
            .toList();
    }

    public static BranchInfo mapToBranchInfo(Map<String, Object> branch) {
        String branchName = (String) branch.get("name");
        String commitSha = (String) ((Map<String, Object>) branch.get("commit")).get("sha");

        return BranchInfo.builder()
            .branchName(branchName)
            .lastCommitSHA(commitSha)
            .build();
    }

    public static boolean isNotFork(Map<String, Object> repo) {
        Object forkObj = repo.get("fork");
        return !((boolean) forkObj);
    }
}
