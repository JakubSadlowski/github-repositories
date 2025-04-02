package org.js.githubrepos.api.model;

import lombok.Data;

import java.util.List;

@Data
public class RepositoryInfo {
    private String repositoryName;
    private String ownerLogin;
    private List<BranchInfo> branches;
}
