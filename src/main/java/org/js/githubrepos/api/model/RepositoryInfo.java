package org.js.githubrepos.api.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RepositoryInfo {
    private String repositoryName;
    private String ownerLogin;
    private List<BranchInfo> branches;
}
