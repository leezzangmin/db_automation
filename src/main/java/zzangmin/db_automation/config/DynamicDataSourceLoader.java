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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
@DependsOn("profileUtil")
public class DynamicDataSourceLoader {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final AwsService awsService;

    private final static String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private final static String ENDPOINT_DRIVER_PREFIX = "jdbc:mysql://";

    @PostConstruct
    public void loadDynamicDataSources() {
        Map<String, List<DBCluster>> clusters = awsService.findAllClusterInfo();
        Map<String, List<DBInstance>> instances = awsService.findAllInstanceInfo();

        Map<String, DatabaseConnectionInfo> clusterMap = loadAwsClusters(clusters);
        Map<String, DatabaseConnectionInfo> instanceMap = loadAwsInstances(instances);
        Map<String, DatabaseConnectionInfo> dbMap = loadIdcInstances();

        clusterMap.forEach(dynamicDataSourceProperties::addDatabase);
        instanceMap.forEach(dynamicDataSourceProperties::addDatabase);
        dbMap.forEach(dynamicDataSourceProperties::addDatabase);

        dynamicDataSourceProperties.validateDatabases();
        dynamicDataSourceProperties.logDatabases();
    }

    private Map<String, DatabaseConnectionInfo> loadIdcInstances() {
        Map<String, DatabaseConnectionInfo> dbMap = new HashMap<>();

        return dbMap;
    }

    private Map<String, DatabaseConnectionInfo> loadAwsInstances(Map<String, List<DBInstance>> instances) {
        Map<String, DatabaseConnectionInfo> instanceMap = new HashMap<>();
        for (String accountId : instances.keySet()) {
            List<DBInstance> accountInstances = instances.get(accountId);
            for (DBInstance accountInstance : accountInstances) {
                String dbName = accountInstance.dbInstanceIdentifier();
                List<Tag> tags = accountInstance.tagList();
                if (!isValidTags(dbName, tags)) {
                    continue;
                }
                Tag serviceNameTag = tags.stream()
                        .filter(tag -> tag.key().equals(TagStandard.SERVICE_TAG_KEY_NAME))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
                Tag environmentTag = tags.stream()
                        .filter(tag -> tag.key().equals(TagStandard.ENVIRONMENT_TAG_KEY_NAME))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);

                String rdsUsername = awsService.findRdsUsername(accountId, serviceNameTag.value(), environmentTag.value());
                String password = awsService.findRdsPassword(accountId, serviceNameTag.value(), environmentTag.value());

                DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                        .environment(environmentTag.value())
                        .accountId(accountId)
                        .serviceName(serviceNameTag.value())
                        .databaseType(DatabaseConnectionInfo.DatabaseType.INSTANCE)
                        .databaseName(dbName)
                        .driverClassName(DRIVER_CLASS_NAME)
                        .writerEndpoint(ENDPOINT_DRIVER_PREFIX + accountInstance.endpoint().address())
                        .readerEndpoint(ENDPOINT_DRIVER_PREFIX + accountInstance.endpoint().address())
                        .username(rdsUsername)
                        .password(password)
                        .build();
                instanceMap.put(dbName, databaseConnectionInfo);
            }
        }
        return instanceMap;
    }

    private Map<String, DatabaseConnectionInfo> loadAwsClusters(Map<String, List<DBCluster>> clusters) {
        Map<String, DatabaseConnectionInfo> clusterMap = new HashMap<>();
        for (String accountId : clusters.keySet()) {
            List<DBCluster> accountClusters = clusters.get(accountId);
            for (DBCluster accountCluster : accountClusters) {
                String dbName = accountCluster.dbClusterIdentifier();

                List<Tag> tags = accountCluster.tagList();
                if (!isValidTags(dbName, tags)) {
                    continue;
                }
                Tag serviceNameTag = tags.stream()
                        .filter(tag -> tag.key().equals(TagStandard.SERVICE_TAG_KEY_NAME))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
                Tag environmentTag = tags.stream()
                        .filter(tag -> tag.key().equals(TagStandard.ENVIRONMENT_TAG_KEY_NAME))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);

                String rdsUsername = awsService.findRdsUsername(accountId, serviceNameTag.value(), environmentTag.value());
                String password = awsService.findRdsPassword(accountId, serviceNameTag.value(), environmentTag.value());

                DatabaseConnectionInfo databaseConnectionInfo = DatabaseConnectionInfo.builder()
                        .environment(environmentTag.value())
                        .accountId(accountId)
                        .serviceName(serviceNameTag.value())
                        .databaseType(DatabaseConnectionInfo.DatabaseType.CLUSTER)
                        .databaseName(dbName)
                        .driverClassName(DRIVER_CLASS_NAME)
                        .writerEndpoint(ENDPOINT_DRIVER_PREFIX + accountCluster.endpoint())
                        .readerEndpoint(ENDPOINT_DRIVER_PREFIX + accountCluster.readerEndpoint())
                        .username(rdsUsername)
                        .password(password)
                        .build();
                clusterMap.put(dbName, databaseConnectionInfo);
            }
        }
        return clusterMap;
    }

    private boolean isValidTags(String dbName, List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            log.info("{} DB에 태그가 존재하지 않습니다.", dbName);
            return false;
        }
        return true;
    }
}
