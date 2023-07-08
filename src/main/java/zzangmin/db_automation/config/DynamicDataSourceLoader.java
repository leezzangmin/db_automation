package zzangmin.db_automation.config;


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

    public void loadDynamicDataSources() {
        DescribeDbClustersResponse response = awsService.findAllClusterInfo();
        List<DBCluster> dbClusters = response.dbClusters();

        for (DBCluster cluster : dbClusters) {
            String dbName = cluster.dbClusterIdentifier();
            String password = awsService.findRdsPassword(dbName);
            log.info("dbcluster: {} ", cluster);
            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(dbName)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + cluster.endpoint())
                    .username(cluster.masterUsername())
                    .password(password)
                    .build();

            dynamicDataSourceProperties.addDatabase(dbName, databaseConnectionInfo);
        }
        dynamicDataSourceProperties.displayDatabases();
    }
}
