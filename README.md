# github-repositories-rest-service

# Overview

This service is used to call Github REST API 2022-11-28 version and list all non-forked repositories, 
all branches and their last commit sha for the given Github login.

## Environments

| Environment | Address                 |
|-------------|-------------------------|
| DEV         | https://localhost:8080/ |

# Swagger API live documentation

| Environment | Address                                |
|-------------|----------------------------------------|
| DEV         | https://localhost:8080/swagger-ui.html |

# API Endpoints and methods

| URIs                                        | Summary                                                                                            | GET                | PUT                | POST | DEL                |
|---------------------------------------------|----------------------------------------------------------------------------------------------------|--------------------|--------------------|------|--------------------|
| `/v1/github-repos/{githubLogin}`            | Returns all non-forked repositories, their branches, and last commit's sha for given Github login. | :heavy_check_mark: |                    |      |                    |

## Getting Started

### Requirements

- Java 21
- Maven installed

### Github Token Setup

To get permission for the request from Github REST API and increase your rate limit 
you need to specify proper Github token in the request.

To achieve this these are the steps to add your token to the service:

### Windows
Add your token to the environment variable for the terminal session.
```bash
  $env:GITHUB_TOKEN="your_github_token_here"
```

To add it permanently.
```bash
  setx GITHUB_TOKEN "your_github_token_here"
```

### Build and Run

```bash
  mvn clean install
```