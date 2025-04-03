package org.js.githubrepos.api;

import org.js.githubrepos.service.GithubReposService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1/")
public class GithubRepositoriesController {
    private final GithubReposService githubReposService;

    @Autowired
    public GithubRepositoriesController(GithubReposService githubReposService) {
        this.githubReposService = githubReposService;
    }

    @GetMapping("github-repos/{githubLogin}")
    public ResponseEntity<String> getGithubRepositoriesInfo(@PathVariable("githubLogin") String githubLogin) throws IOException {
        return ResponseEntity.ok(githubReposService.executeListRepositoriesForUserEndpoint(githubLogin));
    }
}
