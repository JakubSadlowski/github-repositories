package org.js.githubrepos.service;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GithubReposServiceTest {

    private MockWebServer mockWebServer;
    private GithubReposService githubReposService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        OkHttpClient client = new OkHttpClient();
        githubReposService = new GithubReposService(client);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCallGithubApiAndReturnRepositoriesForUser() throws IOException, InterruptedException {
        // given
        String username = "testuser";
        String mockResponseBody = "[{\"name\":\"repo1\",\"full_name\":\"testuser/repo1\"},{\"name\":\"repo2\",\"full_name\":\"testuser/repo2\"}]";

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(mockResponseBody));

        // when
        String responseBody = githubReposService.executeListRepositoriesForUserEndpoint(
            mockWebServer.url("").toString(), username);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/users/" + username + "/repos", recordedRequest.getPath());
        assertEquals("application/vnd.github.v3+json", recordedRequest.getHeader("Accept"));
        //assertEquals("token test-token", recordedRequest.getHeader("Authorization"));
        assertEquals(mockResponseBody, responseBody);
    }
}