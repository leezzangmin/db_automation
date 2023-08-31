package zzangmin.db_automation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.HashMap;
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

    public void logDatabases() {
        for (String databaseName : databases.keySet()) {
            log.info("databases: {}", databases.get(databaseName));
        }
    }
}