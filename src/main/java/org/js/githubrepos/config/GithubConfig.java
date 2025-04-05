package org.js.githubrepos.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubConfig {

    public String getUrlOfGithubServer() {
        return "https://api.github.com";
    }

}
