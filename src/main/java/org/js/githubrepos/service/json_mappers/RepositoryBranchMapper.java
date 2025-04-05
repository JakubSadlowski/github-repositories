package org.js.githubrepos.service.json_mappers;

import org.js.githubrepos.api.model.BranchInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepositoryBranchMapper {

    public static List<BranchInfo> mapToBranchesList(List<Map<String, Object>> repositoryBranchesRaw) {
        List<BranchInfo> branchesList = new ArrayList<>();
        for (Map<String, Object> branchMap : repositoryBranchesRaw) {
            BranchInfo branchInfo = mapToBranchInfo(branchMap);
            branchesList.add(branchInfo);
        }
        return branchesList;
    }

    private static BranchInfo mapToBranchInfo(Map<String, Object> branch) {
        String branchName = (String) branch.get("name");
        String commitSha = (String) ((Map<String, Object>) branch.get("commit")).get("sha");

        return BranchInfo.builder()
            .branchName(branchName)
            .lastCommitSHA(commitSha)
            .build();
    }
}
