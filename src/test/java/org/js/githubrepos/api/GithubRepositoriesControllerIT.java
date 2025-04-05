package org.js.githubrepos.api;

import lombok.extern.apachecommons.CommonsLog;
import org.jetbrains.annotations.NotNull;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.GithubRepositoryResponse;
import org.js.githubrepos.api.model.RepositoryInfo;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnRepositoriesWhenUserExists() {
        // Given
        String url = makeUrl("JakubSadlowski");

        // When
        HttpEntity<Void> entity = new HttpEntity<>(null, new HttpHeaders());
        ResponseEntity<GithubRepositoryResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });
        log.debug(response.getBody());

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<RepositoryInfo> repositories = response.getBody()
            .getRepositoryList();
        assertNotNull(repositories);
        assertFalse(repositories.isEmpty());
        for (RepositoryInfo repository : repositories) {
            assertNotNull(repository.getRepositoryName());
            assertNotNull(repository.getOwnerLogin());
            List<BranchInfo> branches = repository.getBranches();
            assertFalse(branches.isEmpty());
            for (BranchInfo branch : branches) {
                assertNotNull(branch.getBranchName());
                assertNotNull(branch.getLastCommitSHA());
            }
        }
    }

    @NotNull
    private static String makeUrl(String githubLogin) {
        return UriComponentsBuilder.fromPath(GET_URL_TO_TEST)
            .buildAndExpand(githubLogin)
            .toUriString();
    }

}