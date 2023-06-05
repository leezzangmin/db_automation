package zzangmin.db_automation.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.client.AwsClient;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DynamicDataSourceLoader {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final AwsClient awsClient;

    @PostConstruct
    public void loadDynamicDataSources() {
        RdsClient rdsClient = awsClient.getRdsClient();

        DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder().build();
        DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
        List<DBInstance> dbInstances = response.dbInstances();

        // RDS 클러스터 정보를 DynamicDataSourceProperties 에 추가
        for (DBInstance instance : dbInstances) {
            String dbname = instance.dbInstanceIdentifier();

            DatabaseConfig databaseConfig = DatabaseConfig.builder()
                    .databaseName(instance.dbInstanceIdentifier())
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url("jdbc:mysql://" + instance.endpoint().address())
                    .username(instance.masterUsername())
                    .password("12345678")
                    .build();

            // TODO: dbName 변경
            dynamicDataSourceProperties.addDatabase(dbname, databaseConfig);
        }
    }
}
