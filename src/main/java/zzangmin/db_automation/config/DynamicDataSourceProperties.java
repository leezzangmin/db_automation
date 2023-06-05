package zzangmin.db_automation.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamicDataSourceProperties {

    private Map<String, DatabaseConfig> databases = new HashMap();

    public DatabaseConfig findByDbName(String dbName) {
        return databases.get(dbName);
    }
    public void addDatabase(String dbName, DatabaseConfig databaseConfig) {
        databases.put(dbName, databaseConfig);
    }
}