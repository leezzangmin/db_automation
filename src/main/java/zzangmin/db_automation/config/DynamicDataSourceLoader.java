package zzangmin.db_automation.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
@DependsOn("profileUtil")
public class DynamicDataSourceLoader {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final AwsService awsService;

    @PostConstruct
    public void loadDynamicDataSources() {
        List<DBCluster> dbClusters = awsService.findAllClusterInfo();
        List<DBInstance> instances = awsService.findAllInstanceInfo();

        for (DBCluster cluster : dbClusters) {
            String dbName = cluster.dbClusterIdentifier();
            List<Tag> tags = cluster.tagList();
            if (!isValidTags(dbName, tags)) {
                continue;
            }
            Tag serviceNameTag = tags.stream()
                    .filter(tag -> tag.key().equals(TagStandard.SERVICE_TAG_KEY_NAME))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            String rdsUsername = awsService.findRdsUsername(serviceNameTag.value());
            String password = awsService.findRdsPassword(serviceNameTag.value());

            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + cluster.endpoint())
                    .username(rdsUsername)
                    .password(password)
                    .tags(tags.stream()
                            .map(t -> new DatabaseConnectionInfo.Tag(t.key(), t.value()))
                            .collect(Collectors.toList()))
                    .build();

            dynamicDataSourceProperties.addDatabase(dbName, databaseConnectionInfo);
        }

        for (DBInstance instance : instances) {
            String dbName = instance.dbInstanceIdentifier();
            List<Tag> tags = instance.tagList();
            if (!isValidTags(dbName, tags)) {
                continue;
            }
            String rdsUsername = awsService.findRdsUsername(dbName);
            String password = awsService.findRdsPassword(dbName);

            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + instance.endpoint().address())
                    .username(rdsUsername)
                    .password(password)
                    .tags(tags.stream()
                            .map(t -> new DatabaseConnectionInfo.Tag(t.key(), t.value()))
                            .collect(Collectors.toList()))
                    .build();
            dynamicDataSourceProperties.addDatabase(dbName, databaseConnectionInfo);
        }
        dynamicDataSourceProperties.validateDatabases();
        dynamicDataSourceProperties.logDatabases();
    }

    private boolean isValidTags(String dbName, List<Tag> tags) {
        if (tags.size() == 0) {
            log.info("{} DB에 필수 태그가 존재하지 않습니다.", dbName);
            return false;
        }
        return true;
    }
}
