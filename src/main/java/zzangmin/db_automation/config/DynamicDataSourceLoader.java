package zzangmin.db_automation.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
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
            String password = awsService.findRdsPassword(dbName);
            log.info("dbCluster: {} ", cluster);
            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + cluster.endpoint())
                    .username(cluster.masterUsername())
                    .password(password)
                    .build();

            dynamicDataSourceProperties.addDatabase(dbName, databaseConnectionInfo);
        }

        for (DBInstance instance : instances) {
            String dbName = instance.dbInstanceIdentifier();
            String password = awsService.findRdsPassword(dbName);
            log.info("dbInstance: {} ", instance);
            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + instance.endpoint().address())
                    .username(instance.masterUsername())
                    .password(password)
                    .build();
            dynamicDataSourceProperties.addDatabase(dbName, databaseConnectionInfo);
        }


        dynamicDataSourceProperties.displayDatabases();
    }
}
