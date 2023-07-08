package zzangmin.db_automation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DynamicDataSourceProperties {

    private Map<String, DatabaseConnectionInfo> databases = new HashMap();

    public DatabaseConnectionInfo findByDbName(String dbName) {
        return databases.get(dbName);
    }

    public void addDatabase(String dbName, DatabaseConnectionInfo databaseConnectionInfo) {
        databases.put(dbName, databaseConnectionInfo);
    }

    public void displayDatabases() {
        for (String databaseName : databases.keySet()) {
            log.info("databases: {}", databases.get(databaseName));
        }
    }
}