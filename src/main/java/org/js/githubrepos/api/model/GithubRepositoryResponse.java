package org.js.githubrepos.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryResponse {
    List<RepositoryInfo> repositoryList;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (RepositoryInfo repository : repositoryList) {
            sb.append("repository=")
                .append(repository.getRepositoryName())
                .append(", owner=")
                .append(repository.getOwnerLogin())
                .append("\n");
            for (BranchInfo branch : repository.getBranches()) {
                sb.append("\tbranch=")
                    .append(branch.getBranchName())
                    .append(",lastCommitSHA=")
                    .append(branch.getLastCommitSHA())
                    .append("\n");
            }
        }
        return sb.toString();
    }
}
