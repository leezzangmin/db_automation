package zzangmin.db_automation.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.standardcheck.TagStandardChecker;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;
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
            if (!isClusterStatusAvailable(cluster)) {
                continue;
            }
            String dbName = cluster.dbClusterIdentifier();
            List<Tag> tags = awsService.findRdsTagsByArn(cluster.dbClusterArn());
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
            if (!isInstanceStatusAvailable(instance)) {
                continue;
            }
            String dbName = instance.dbInstanceIdentifier();
            List<Tag> tags = awsService.findRdsTagsByArn(instance.dbInstanceArn());
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
        dynamicDataSourceProperties.logDatabases();
    }

    private boolean isValidTags(String dbName, List<Tag> tags) {
        if (tags.size() == 0) {
            log.info("{} DB에 필수 태그가 존재하지 않습니다.", dbName);
            return false;
        } else if (!TagStandardChecker.isCurrentEnvHasValidTag(tags)) {
            return false;
        }
        return true;
    }

    private boolean isClusterStatusAvailable(DBCluster cluster) {
        if (cluster.status().equals("available")) {
            return true;
        }
        return false;
    }

    private boolean isInstanceStatusAvailable(DBInstance instance) {
        if (instance.dbInstanceStatus().equals("available")) {
            return true;
        }
        return false;
    }

}
