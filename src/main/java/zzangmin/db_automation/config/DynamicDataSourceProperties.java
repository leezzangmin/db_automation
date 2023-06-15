package zzangmin.db_automation.config;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

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

    public void displayDatabases() {
        System.out.println("databases: ");
        for (String databaseName : databases.keySet()) {
            System.out.println(databases.get(databaseName));
        }
    }
}