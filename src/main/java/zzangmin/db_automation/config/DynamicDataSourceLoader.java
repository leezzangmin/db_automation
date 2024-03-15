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
            if (!isClusterStatusAvailable(cluster)) {
                continue;
            }
            String dbName = cluster.dbClusterIdentifier();
            String password = awsService.findRdsPassword(dbName);
            List<Tag> tags = awsService.findRdsTagsByArn(cluster.dbClusterArn());

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
            String password = awsService.findRdsPassword(dbName);
            List<Tag> tags = awsService.findRdsTagsByArn(instance.dbInstanceArn());

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
