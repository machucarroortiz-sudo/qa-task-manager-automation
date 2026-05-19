package com.qataskmanager.automation_sut.dto;

import java.util.List;

public class SutInfoDtos {
    public record SutInfoResponse(
            ApplicationInfo application,
            RuntimeInfo runtime,
            DatabaseInfo database,
            HostInfo host
    ) {
    }

    public record ApplicationInfo(
            String displayName,
            String version,
            String releaseName,
            String lifecycleStage,
            String description,
            List<String> activeProfiles
    ) {
    }

    public record RuntimeInfo(
            String javaVersion,
            String springBootVersion,
            String server,
            String timezone
    ) {
    }

    public record DatabaseInfo(
            String product,
            String version,
            String driver
    ) {
    }

    public record HostInfo(
            String operatingSystem,
            String architecture
    ) {
    }
}
