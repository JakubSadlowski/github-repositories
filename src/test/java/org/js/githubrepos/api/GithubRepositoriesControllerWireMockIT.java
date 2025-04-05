package org.js.githubrepos.api;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.Builder;
import lombok.extern.apachecommons.CommonsLog;
import org.jetbrains.annotations.NotNull;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.js.githubrepos.config.GithubConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@CommonsLog
class GithubRepositoriesControllerWireMockIT {
    private static final int WIRE_MOCK_PORT = 8089;
    private static final String TEST_SERVER = "";
    private static final String WIREMOCK_TEST_SERVER = "http://localhost:" + WIRE_MOCK_PORT;
    private static final String GET_URL_TO_TEST = "/v1/github-repos/{login}";

    @MockBean
    private GithubConfig githubConfig; // Mocked instance

    @InjectMocks
    private GithubRepositoriesController githubRepositoriesController; // Controller with mock injected

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig()
            .port(WIRE_MOCK_PORT))
        .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setup() {
        MockGithub.builder()
            .wireMock(wireMock)
            .build()
            .mockResponses();
    }

    @Test
    void checkoutInvalidRequestWithTwoTheSameItems_returnsBadRequest() {
        // Given
        String url = makeUrl("nonExistingUser");
        when(githubConfig.getUrlOfGithubServer()).thenReturn(WIREMOCK_TEST_SERVER);
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnRepositoriesWhenUserExists() {
        // Given
        String url = makeUrl("JakubSadlowski");
        when(githubConfig.getUrlOfGithubServer()).thenReturn(WIREMOCK_TEST_SERVER);
        // When
        HttpEntity<Void> entity = new HttpEntity<>(null, new HttpHeaders());
        ResponseEntity<List<RepositoryInfo>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
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

    @NotNull
    private static String makeUrl(String login) {
        return TEST_SERVER + UriComponentsBuilder.fromPath(GET_URL_TO_TEST)
            .buildAndExpand(login)
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

    @Builder
    private static class MockGithub {
        private final WireMockExtension wireMock;

        private MockGithub(WireMockExtension wireMock) {
            this.wireMock = wireMock;
        }

        void mockResponses() {
            mockGetRepositoriesForNonExistingGithubUser();
            mockGetRepositoriesForExistingGithubUser();
            mockGetBRanchesForExistingGithubUser();
        }

        private void mockGetBRanchesForExistingGithubUser() {
            stubWireMock(".*/repos/.*/.*/branches", 200, """
                [
                   {
                     "name": "Branch1",
                     "commit": {
                       "sha": "1da4daf47d8fb72ffb2ebb82c43c397cbbba62d0"
                     }
                   },
                   {
                     "name": "Branch2",
                     "commit": {
                       "sha": "1da4daf47d8fb72ffb2ebb82c43c397cbbba62d2"
                     }
                   }
                 ]
                """);
        }

        private void mockGetRepositoriesForExistingGithubUser() {
            stubWireMock(".*/users/(?!nonExistingUser).*?/repos", 200, """
                [
                  {
                    "name": "Repository1",
                    "owner": {
                      "login": "Owner1"
                    },
                    "fork": false
                  },
                  {
                    "name": "Repository2",
                    "owner": {
                      "login": "Owner1"
                    },
                    "fork": true
                  },
                  {
                    "name": "Repository2",
                    "owner": {
                      "login": "Owner2"
                    },
                    "fork": false
                  }
                ]
                """);
        }

        private void mockGetRepositoriesForNonExistingGithubUser() {
            stubWireMock(".*/users/nonExistingUser/repos", 404, "{\"error\": \"Resource not found\"}");
        }

        private void stubWireMock(String url, int httpStatus, String body) {
            wireMock.stubFor(get(urlMatching(url)).willReturn(aResponse().withStatus(httpStatus)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));
        }
    }
}