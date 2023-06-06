package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.config.DatabaseConnectionInfo;
import zzangmin.db_automation.config.DynamicDataSourceProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DDLController {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    @PostMapping("/ddl/validate")
    public String validCommand(@RequestParam String dbName, @RequestParam String ddlCommand) {
        return "ok";
    }

    @PostMapping("/ddl/execute")
    public String executeCommand(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestParam String ddlCommand) {
        StringBuilder result = new StringBuilder();
        DatabaseConnectionInfo databaseConnectionInfo = dynamicDataSourceProperties.findByDbName(dbName);

        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"Cromysql5128*" );//databaseConfig.getPassword());

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ddlCommand);

            while (resultSet.next()) {
                result.append(resultSet.getString(1));
            }

            result.append("DDL executed successfully on database: ").append(databaseConnectionInfo.getDatabaseName());

            statement.close();
            connection.close();
        } catch (Exception e) {
            result.append(e);
            result.append("Failed to execute DDL on database: ").append(databaseConnectionInfo.getDatabaseName());
            result.append("\n").append(e.getMessage());
        }
        return result.toString();
    }
}

