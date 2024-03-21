package zzangmin.db_automation.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DynamicDataSourceLoader {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final AwsService awsService;

    @PostConstruct
    public void loadDynamicDataSources() {
        DescribeDbClustersResponse clusterResponse = awsService.findAllClusterInfo();
        List<DBCluster> dbClusters = clusterResponse.dbClusters();
        List<DBInstance> instances = awsService.findAllInstanceInfo();

        for (DBCluster cluster : dbClusters) {
            String dbName = cluster.dbClusterIdentifier();
            List<Tag> tags = cluster.tagList();
            if (!isValidTags(dbName, tags)) {
                continue;
            }
            String password = awsService.findRdsPassword(dbName);

            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + cluster.endpoint())
                    .username(cluster.masterUsername())
                    .password(password)
                    .tags(tags)
                    .build();

            dynamicDataSourceProperties.addDatabase(dbName, databaseConnectionInfo);
        }

        for (DBInstance instance : instances) {
            String dbName = instance.dbInstanceIdentifier();
            List<Tag> tags = instance.tagList();
            if (!isValidTags(dbName, tags)) {
                continue;
            }
            String password = awsService.findRdsPassword(dbName);

            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + instance.endpoint().address())
                    .username(instance.masterUsername())
                    .password(password)
                    .tags(tags)
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
