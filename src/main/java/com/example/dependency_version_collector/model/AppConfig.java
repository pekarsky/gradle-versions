package com.example.dependency_version_collector.model;

import lombok.Data;

@Data
public class AppConfig {
    private String name;
    private String git_repo_url;
    private String git_project_id;
    private String artifact_id;
    private int startup_seconds_timeout;
    private String build;
    private String deployment_type;
    private String owner_team;
    private boolean critical;
    private boolean high_availability;
    private boolean confidential;
    private String architectural_product;
    private String config_server;
    private String role_name;
    private boolean fake;
}
