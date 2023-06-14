package zzangmin.db_automation.config;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.client.AwsClient;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DynamicDataSourceLoader {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final AwsClient awsClient;

    public void loadDynamicDataSources() {
        RdsClient rdsClient = awsClient.getRdsClient();

        DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder().build();
        DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
        List<DBInstance> dbInstances = response.dbInstances();

        for (DBInstance instance : dbInstances) {
            String dbname = instance.dbInstanceIdentifier();

            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(instance.dbInstanceIdentifier())
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + instance.endpoint().address())
                    .username(instance.masterUsername())
                    .password("12345678")
                    .build();

            dynamicDataSourceProperties.addDatabase(dbname, databaseConnectionInfo);
        }
        dynamicDataSourceProperties.displayDatabases();
    }
}
