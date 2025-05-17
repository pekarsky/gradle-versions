package com.example.gradle_versions.model;

import lombok.Data;

import java.util.List;

@Data
public class AppConfigWrapper {
    private List<AppConfig> apps;
}
