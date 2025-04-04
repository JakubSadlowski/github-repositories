package org.js.githubrepos.api;


import lombok.extern.apachecommons.CommonsLog;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ComponentScan(basePackages = {"org.js.githubrepos.config"})
@CommonsLog
class GithubRepositoriesControllerIT {
    private static final String GET_URL_TO_TEST = "/v1/github-repos/{login}";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void checkoutInvalidRequestWithTwoTheSameItems_returnsBadRequest() {
        // Given
        String login = "nonExistingUser";

        // When
        String url = UriComponentsBuilder.fromPath(GET_URL_TO_TEST).buildAndExpand(login).toUriString();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnRepositoriesWhenUserExists() {
        // Given
        String login = "JakubSadlowski";

        // When
        String url = UriComponentsBuilder.fromPath(GET_URL_TO_TEST).buildAndExpand(login).toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(null, new HttpHeaders());
        ResponseEntity<List<RepositoryInfo>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });
        log.debug(getGithubRepositoriesInfoToString(Objects.requireNonNull(response.getBody())));

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RepositoryInfo> repositories = response.getBody();
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

    private static String getGithubRepositoriesInfoToString(List<RepositoryInfo> repositories) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (RepositoryInfo repository : repositories) {
            sb.append("repository=").append(repository.getRepositoryName())
                    .append(", owner=").append(repository.getOwnerLogin())
                    .append("\n");
            for (BranchInfo branch : repository.getBranches()) {
                sb.append("\tbranch=").append(branch.getBranchName())
                        .append(",lastCommitSHA=").append(branch.getLastCommitSHA())
                        .append("\n");
            }
        }
        return sb.toString();
    }
}