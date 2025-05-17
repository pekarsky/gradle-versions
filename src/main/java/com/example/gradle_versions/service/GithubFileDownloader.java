package com.example.gradle_versions.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class GithubFileDownloader {

    @Value("${githubToken}")
    private String githubToken;
    @Value("${githubPathTemplate}")
    private String githubPathTemplate;

    public String getFileContent(String repo, String branch, String filePath) throws Exception {
        String fileUrl = String.format(githubPathTemplate, repo, branch, filePath);
        return downloadFileFromGitHub(fileUrl, githubToken);
    }

    private String downloadFileFromGitHub(String fileUrl, String token) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + token);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to download file: HTTP error code " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
}
