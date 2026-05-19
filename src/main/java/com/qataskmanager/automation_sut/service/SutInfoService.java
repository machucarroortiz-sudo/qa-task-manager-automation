package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.config.AppMetadataProperties;
import com.qataskmanager.automation_sut.dto.SutInfoDtos.ApplicationInfo;
import com.qataskmanager.automation_sut.dto.SutInfoDtos.DatabaseInfo;
import com.qataskmanager.automation_sut.dto.SutInfoDtos.HostInfo;
import com.qataskmanager.automation_sut.dto.SutInfoDtos.RuntimeInfo;
import com.qataskmanager.automation_sut.dto.SutInfoDtos.SutInfoResponse;
import java.time.ZoneId;
import java.util.Arrays;
import javax.sql.DataSource;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SutInfoService implements EnvironmentAware {
    private final AppMetadataProperties metadata;
    private final DataSource dataSource;
    private Environment environment;

    public SutInfoService(AppMetadataProperties metadata, DataSource dataSource) {
        this.metadata = metadata;
        this.dataSource = dataSource;
    }

    public SutInfoResponse getSutInfo() {
        return new SutInfoResponse(
                applicationInfo(),
                runtimeInfo(),
                databaseInfo(),
                hostInfo()
        );
    }

    private ApplicationInfo applicationInfo() {
        return new ApplicationInfo(
                metadata.getDisplayName(),
                metadata.getVersion(),
                metadata.getReleaseName(),
                metadata.getLifecycleStage(),
                metadata.getDescription(),
                Arrays.asList(environment.getActiveProfiles())
        );
    }

    private RuntimeInfo runtimeInfo() {
        return new RuntimeInfo(
                System.getProperty("java.version"),
                SpringBootVersion.getVersion(),
                "Spring Boot embedded web server",
                ZoneId.systemDefault().toString()
        );
    }

    private DatabaseInfo databaseInfo() {
        try (var connection = dataSource.getConnection()) {
            var metadata = connection.getMetaData();
            return new DatabaseInfo(
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    metadata.getDriverName() + " " + metadata.getDriverVersion()
            );
        } catch (Exception exception) {
            return new DatabaseInfo("Unavailable", "Unavailable", "Unavailable");
        }
    }

    private HostInfo hostInfo() {
        return new HostInfo(
                System.getProperty("os.name") + " " + System.getProperty("os.version"),
                System.getProperty("os.arch")
        );
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
