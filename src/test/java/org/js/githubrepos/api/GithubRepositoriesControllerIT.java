package org.js.githubrepos.api;

import lombok.extern.apachecommons.CommonsLog;
import org.jetbrains.annotations.NotNull;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.GithubRepositoryResponse;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CommonsLog
class GithubRepositoriesControllerIT {
    private static final String GET_URL_TO_TEST = "/v1/github-repos/{login}";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnNotFoundStatusWhenUserDoesNotExist() {
        // Given
        String url = makeUrl("loginNotExisting");

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnRepositoriesWhenUserExists() {
        // Given
        String url = makeUrl("JakubSadlowski");

        // When
        HttpEntity<Void> entity = new HttpEntity<>(null, new HttpHeaders());
        ResponseEntity<GithubRepositoryResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });
        log.debug(getGithubRepositoriesInfoToString(Objects.requireNonNull(response.getBody().getRepositoryList())));

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RepositoryInfo> repositories = response.getBody().getRepositoryList();
        Assertions.assertNotNull(repositories);
        Assertions.assertFalse(repositories.isEmpty());
        for (RepositoryInfo repository : repositories) {
            Assertions.assertNotNull(repository.getRepositoryName());
            Assertions.assertNotNull(repository.getOwnerLogin());
            List<BranchInfo> branches = repository.getBranches();
            Assertions.assertFalse(branches.isEmpty());
            for (BranchInfo branch : branches) {
                Assertions.assertNotNull(branch.getBranchName());
                Assertions.assertNotNull(branch.getLastCommitSHA());
            }
        }
    }

    @NotNull
    private static String makeUrl(String githubLogin) {
        return UriComponentsBuilder.fromPath(GET_URL_TO_TEST)
            .buildAndExpand(githubLogin)
            .toUriString();
    }

    private static String getGithubRepositoriesInfoToString(List<RepositoryInfo> repositories) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (RepositoryInfo repository : repositories) {
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