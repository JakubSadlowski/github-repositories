package org.js.githubrepos.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/")
public class GithubRepositoriesController {

    @GetMapping("github-repos/{githubLogin}")
    public ResponseEntity<String> getAllSwiftCodesForSpecificCountry(@PathVariable("githubLogin") String githubLogin) {
        return ResponseEntity.ok("Hello World!");
    }
}
