package zzangmin.db_automation.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamicDataSourceProperties {

    private Map<String, DatabaseConnectionInfo> databases = new HashMap();

    public DatabaseConnectionInfo findByDbName(String dbName) {
        return databases.get(dbName);
    }
    public void addDatabase(String dbName, DatabaseConnectionInfo databaseConnectionInfo) {
        databases.put(dbName, databaseConnectionInfo);
    }
}