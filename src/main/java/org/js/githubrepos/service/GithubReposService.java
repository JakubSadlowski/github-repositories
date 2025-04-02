package org.js.githubrepos.service;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GithubReposService {
    private static final String GITHUB_API_URL_BASE = "https://api.github.com";

    private OkHttpClient okHttpClient;

    @Autowired
    public GithubReposService(OkHttpClient client) {
        this.okHttpClient = client;
    }


}
