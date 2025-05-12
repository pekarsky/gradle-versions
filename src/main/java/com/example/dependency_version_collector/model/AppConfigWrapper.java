package com.example.dependency_version_collector.model;

import java.util.List;

public class AppConfigWrapper {
    private List<AppConfig> apps;

    public List<AppConfig> getApps() {
        return apps;
    }

    public void setApps(List<AppConfig> apps) {
        this.apps = apps;
    }
}
