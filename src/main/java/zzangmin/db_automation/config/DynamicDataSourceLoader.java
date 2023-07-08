package zzangmin.db_automation.config;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DynamicDataSourceLoader {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final AwsService awsService;

    public void loadDynamicDataSources() {
        DescribeDbInstancesResponse response = awsService.findAllRdsInstanceInfo();
        List<DBInstance> dbInstances = response.dbInstances();

        for (DBInstance instance : dbInstances) {
            String dbname = instance.dbInstanceIdentifier();
            String password = awsService.findRdsPassword(dbname);

            DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                    .databaseName(instance.dbInstanceIdentifier())
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + instance.endpoint().address())
                    .username(instance.masterUsername())
                    .password(password)
                    .build();

            dynamicDataSourceProperties.addDatabase(dbname, databaseConnectionInfo);
        }
        dynamicDataSourceProperties.displayDatabases();
    }
}
