package zzangmin.db_automation.client;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class MysqlClient {

    public String executeSQL(DatabaseConnectionInfo databaseConnectionInfo, String SQL) {
        StringBuilder result = new StringBuilder();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            statement.execute(SQL);
            result.append("DDL executed successfully on database: ").append(databaseConnectionInfo.getDatabaseName());
            statement.close();
            connection.close();
        } catch (Exception e) {
            result.append(e.getStackTrace());
            result.append("Failed to execute DDL on database: ").append(databaseConnectionInfo.getDatabaseName());
            result.append("\n").append(e.getMessage());
        }
        return result.toString();
    }
}
