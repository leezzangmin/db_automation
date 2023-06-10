package zzangmin.db_automation.client;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.MysqlProcess;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.sql.*;
import java.util.*;

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

    public Set<String> findSchemaNames(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SHOW DATABASES";
        Set<String> schemaNames = new HashSet<>();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                schemaNames.add(resultSet.getString(1));
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schemaNames;
    }

    public List<MysqlProcess> findLongQueries(DatabaseConnectionInfo databaseConnectionInfo, int longQueryStandard) {
        String SQL = "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST WHERE COMMAND = 'Query' AND TIME >= " + longQueryStandard;
        List<MysqlProcess> longQueries = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                long id = resultSet.getLong("ID");
                String user = resultSet.getString("USER");
                String host = resultSet.getString("HOST");
                String db = resultSet.getString("DB");
                String command = resultSet.getString("COMMAND");
                int time = resultSet.getInt("TIME");
                String state = resultSet.getString("STATE");
                String info = resultSet.getString("INFO");
                longQueries.add(new MysqlProcess(id, user, host, db, command, time, state, info));
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return longQueries;
    }

    public String findCreateTableStatement(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SHOW CREATE TABLE `" + schemaName + "`.`" + tableName + "`";
        String createTableStatement = "";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            try (PreparedStatement stmt = connection.prepareStatement(SQL);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    createTableStatement = rs.getString(2);
                    System.out.println(createTableStatement);
                } else {
                    System.out.println("Table not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return createTableStatement;
    }
}
