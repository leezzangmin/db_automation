package zzangmin.db_automation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DynamicDataSourceProperties {

    private static Map<String, DatabaseConnectionInfo> databases = new ConcurrentHashMap<>();

    public DatabaseConnectionInfo findByDbName(String dbName) {
        return databases.get(dbName);
    }

    // 같은 패키지에 속한 DynamicDataSourceLoader 에서만 접근 가능한 메서드 (package-private)
    void addDatabase(String dbName, DatabaseConnectionInfo databaseConnectionInfo) {
        databases.put(dbName, databaseConnectionInfo);
    }

    public static Map<String, DatabaseConnectionInfo> getDatabases() {
        return new HashMap<String, DatabaseConnectionInfo>(databases);
    }

    // prod <-> stage 간 Map 으로 짝지어주는 메서드
    public static Map<DatabaseConnectionInfo, DatabaseConnectionInfo> matchPairDatabase() {
        List<DatabaseConnectionInfo> databases = (List<DatabaseConnectionInfo>) DynamicDataSourceProperties.databases.values();
        Map<String, DatabaseConnectionInfo> prodMap = new HashMap<>();
        Map<String, DatabaseConnectionInfo> stageMap = new HashMap<>();

        // 각 env에 따른 DatabaseConnectionInfo 분류
        databases.forEach(dbInfo -> {
            String serviceValue = dbInfo.getTags().stream()
                    .filter(tag -> tag.key().equals(TagStandard.getServiceTagKeyName()))
                    .findFirst()
                    .map(software.amazon.awssdk.services.rds.model.Tag::value)
                    .orElse(null);
            String envValue = dbInfo.getTags().stream()
                    .filter(tag -> tag.key().equals(TagStandard.getEnvironmentTagKeyName()))
                    .findFirst()
                    .map(software.amazon.awssdk.services.rds.model.Tag::value)
                    .orElse(null);

            if (serviceValue != null && "prod".equals(envValue)) {
                prodMap.put(serviceValue, dbInfo);
            } else if (serviceValue != null && "stage".equals(envValue)) {
                stageMap.put(serviceValue, dbInfo);
            }
        });

        Map<DatabaseConnectionInfo, DatabaseConnectionInfo> matchedDatabases = new HashMap<>();

        // 서비스 이름을 기반으로 prod와 stage DB 매칭
        prodMap.forEach((service, prodDb) -> {
            DatabaseConnectionInfo stageDb = stageMap.get(service);
            if (stageDb != null) {
                matchedDatabases.put(prodDb, stageDb);
            }
        });

        return matchedDatabases;
    }

    public void logDatabases() {
        for (String databaseName : databases.keySet()) {
            log.info("databases: {}", databases.get(databaseName));
        }
    }
}