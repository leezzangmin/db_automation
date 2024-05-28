package zzangmin.db_automation.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class DynamicDataSourceProperties {

    private final MysqlClient mysqlClient;

    private static Map<String, DatabaseConnectionInfo> databases = new ConcurrentHashMap<>();

    public static DatabaseConnectionInfo findByDbName(String dbName) {
        if (dbName == null || dbName == "") {
            throw new IllegalArgumentException("DB 명이 제공되지 않았습니다.");
        }
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
        Collection<DatabaseConnectionInfo> database = DynamicDataSourceProperties.databases.values();
        Map<String, DatabaseConnectionInfo> prodMap = new HashMap<>();
        Map<String, DatabaseConnectionInfo> stageMap = new HashMap<>();

        // 각 env에 따른 DatabaseConnectionInfo 분류
        database.forEach(dbInfo -> {
            String serviceValue = dbInfo.getTags().stream()
                    .filter(tag -> tag.getKey().equals(TagStandard.getServiceTagKeyName()))
                    .findFirst()
                    .map(t -> t.getValue())
                    .orElse(null);
            String envValue = dbInfo.getTags().stream()
                    .filter(tag -> tag.getKey().equals(TagStandard.getEnvironmentTagKeyName()))
                    .findFirst()
                    .map(t -> t.getValue())
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

    public void validateDatabases() {
        if (databases.values().size() == 0) {
            throw new IllegalStateException("로드한 대상 DB가 없습니다.");
        }
        for (DatabaseConnectionInfo databaseConnectionInfo : databases.values()) {
            try {
                mysqlClient.healthCheck(databaseConnectionInfo);
            } catch (Exception e) {
                log.error("헬스체크 실패: {}, {}", databaseConnectionInfo, e.getMessage());
            }
        }
    }


}