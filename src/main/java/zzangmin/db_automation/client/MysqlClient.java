package zzangmin.db_automation.client;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Component
public class MysqlClient {

    public String executeSQL(DatabaseConnectionInfo databaseConnectionInfo, String SQL) {
        StringBuilder result = new StringBuilder();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                result.append(resultSet.getString("table_name"));
            }
            result.append("DDL executed successfully on database: ").append(databaseConnectionInfo.getDatabaseName());
            statement.close();
            connection.close();
        } catch (Exception e) {
            result.append(e.getStackTrace());
            e.printStackTrace();
            result.append("Failed to execute DDL on database: ").append(databaseConnectionInfo.getDatabaseName());
            result.append("\n").append(e.getMessage());
        }
        return result.toString();
    }

    public String executeSQL() {
        return "ok";
    }

    public Set<String> findTableNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema = \"" + schemaName + "\"";
        Set<String> tableNames = new HashSet<>();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("table_name"));
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableNames;
    }

    public List<String> findLongQueries(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST WHERE COMMAND = 'Query' AND TIME >= 0";
        Set<String> LongQueries = new HashSet<>();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                long id = resultSet.getLong("ID");
                String user = resultSet.getString("USER");
                String query = resultSet.getString("INFO");
                System.out.println("ID: " + id + ", User: " + user + ", Query: " + query);
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
