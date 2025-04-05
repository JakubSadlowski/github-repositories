package org.js.githubrepos.api.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class RepositoryInfo {
    private final String repositoryName;
    private final String ownerLogin;
    private final List<BranchInfo> branches = new ArrayList<>();
}
