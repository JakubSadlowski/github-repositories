package org.js.githubrepos.api;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.Builder;
import lombok.extern.apachecommons.CommonsLog;
import org.jetbrains.annotations.NotNull;
import org.js.githubrepos.api.model.BranchInfo;
import org.js.githubrepos.api.model.GithubRepositoryResponse;
import org.js.githubrepos.api.model.RepositoryInfo;
import org.js.githubrepos.config.GithubConfig;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private GithubConfig githubConfig;

    @InjectMocks
    private GithubRepositoriesController githubRepositoriesController;

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
        when(githubConfig.getUrlOfGithubServer()).thenReturn(WIREMOCK_TEST_SERVER);
    }

    @Test
    void shouldReturnNotFoundStatusWhenUserDoesNotExist() {
        // Given
        String url = makeUrl("nonExistingUser");

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnHttp500WhenGithubSiteNotAvailable() {
        // Given
        String url = makeUrl("userForHttp500");
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
        assertEquals(2, repositories.size());
        // And
        RepositoryInfo repository1 = repositories.getFirst();
        assertEquals("Repository1", repository1.getRepositoryName());
        assertEquals("Owner1", repository1.getOwnerLogin());
        List<BranchInfo> repository1Branches = repository1.getBranches();
        assertNotNull(repository1Branches);
        assertEquals(2, repository1Branches.size());
        BranchInfo repository1Branch1 = repository1Branches.getFirst();
        assertEquals("Branch1", repository1Branch1.getBranchName());
        assertEquals("1da4daf47d8fb72ffb2ebb82c43c397cbbba62d0", repository1Branch1.getLastCommitSHA());
        BranchInfo repository1Branch2 = repository1Branches.get(1);
        assertEquals("Branch2", repository1Branch2.getBranchName());
        assertEquals("1da4daf47d8fb72ffb2ebb82c43c397cbbba62d2", repository1Branch2.getLastCommitSHA());
        // And
        RepositoryInfo repository2 = repositories.get(1);
        assertEquals("Repository2", repository2.getRepositoryName());
        assertEquals("Owner2", repository2.getOwnerLogin());
        List<BranchInfo> repository2Branches = repository2.getBranches();
        assertNotNull(repository2Branches);
        assertEquals(2, repository2Branches.size());
    }

    @NotNull
    private static String makeUrl(String login) {
        return TEST_SERVER + UriComponentsBuilder.fromPath(GET_URL_TO_TEST)
            .buildAndExpand(login)
            .toUriString();
    }

    @Builder
    private record MockGithub(WireMockExtension wireMock) {
        void mockResponses() {
            mockHttp500UseCase();
            mockGetRepositoriesForNonExistingGithubUser();
            mockGetRepositoriesForExistingGithubUser();
            mockGetBranchesForExistingGithubUser();
        }

        private void mockGetRepositoriesForNonExistingGithubUser() {
            stubWireMock(".*/users/nonExistingUser/repos", 404, "{\"error\": \"Resource not found\"}");
        }

        private void mockHttp500UseCase() {
            stubWireMock(".*/users/userForHttp500/repos", 500, "{\"error\": \"Internal server error\"}");
        }

        private void mockGetRepositoriesForExistingGithubUser() {
            stubWireMock(".*/users/(?!nonExistingUser|userForHttp500).*?/repos", 200, """
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

        private void mockGetBranchesForExistingGithubUser() {
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

        private void stubWireMock(String url, int httpStatus, String body) {
            wireMock.stubFor(get(urlMatching(url)).willReturn(aResponse().withStatus(httpStatus)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));
        }
    }
}