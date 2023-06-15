package zzangmin.db_automation.client;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.MysqlProcess;
import zzangmin.db_automation.entity.TableStatus;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.sql.*;
import java.util.*;

@Component
public class MysqlClient {

    private static final int COMMAND_TIMEOUT_SECONDS = 600;


    public String executeSQL(DatabaseConnectionInfo databaseConnectionInfo, String SQL) {
        StringBuilder result = new StringBuilder();
        System.out.println("SQL = " + SQL);
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(COMMAND_TIMEOUT_SECONDS);
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

    // TODO: SQL 조건문 추가
    public List<MysqlProcess> findLongQueries(DatabaseConnectionInfo databaseConnectionInfo, int longQueryStandard) {
        String SQL = "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST " +
                "WHERE COMMAND = 'Query' " +
                "AND USER NOT IN ('rdsadmin', 'event_scheduler') " +
                "AND TIME >= " + longQueryStandard;
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
                long time = resultSet.getLong("TIME");
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return createTableStatement;
    }

    public TableStatus findTableStatus(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT TABLE_NAME, TABLE_SCHEMA, TABLE_TYPE, ENGINE, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, CREATE_TIME, UPDATE_TIME " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = '" + schemaName +
                "' AND TABLE_NAME = '" + tableName + "'";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);

            if (resultSet.next()) {
                String schema = resultSet.getString("TABLE_SCHEMA");
                String table = resultSet.getString("TABLE_NAME");
                String type = resultSet.getString("TABLE_TYPE");
                String engine = resultSet.getString("ENGINE");
                int rows = resultSet.getInt("TABLE_ROWS");
                long dataLength = resultSet.getLong("DATA_LENGTH");
                long indexLength = resultSet.getLong("INDEX_LENGTH");
                String createTime = resultSet.getString("CREATE_TIME");
                String updateTime = resultSet.getString("UPDATE_TIME");
                return new TableStatus(schema, table, type, engine, rows, dataLength, indexLength, createTime, updateTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("테이블 정보를 불러올 수 없습니다.");
    }

    public Map<String, List<String>> findIndexes(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT INDEX_NAME, COLUMN_NAME " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = '" + schemaName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY INDEX_NAME, SEQ_IN_INDEX";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);

            Map<String, List<String>> constraints = new HashMap<>();
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                if (constraints.containsKey(indexName)) {
                    List<String> columns = constraints.get(indexName);
                    columns.add(columnName);
                    continue;
                }
                List<String> columns = new ArrayList<>();
                columns.add(columnName);
                constraints.put(indexName, columns);
            }
            return constraints;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("인덱스 정보를 불러올 수 없습니다.");
    }

    public Column findColumn(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName, String columnName) {
        String SQL = "SHOW COLUMNS FROM `" + schemaName + "`.`" + tableName + "` WHERE Field = '" + columnName + "'";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");
            try (PreparedStatement stmt = connection.prepareStatement(SQL);
                 ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    String findColumnName = resultSet.getString("Field");
                    String type = resultSet.getString("Type");
                    String isNull = resultSet.getString("Null");
                    String key = resultSet.getString("Key");
                    String defaultValue = resultSet.getString("Default");
                    String extra = resultSet.getString("Extra");
                    return new Column(findColumnName, type, isNull.equals("NO") ? true : false, defaultValue, key.equals("UNI") ? true : false, extra.equals("auto_increment") ? true : false, null, null, null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("컬럼 정보를 불러올 수 없습니다.");
    }

    public List<MysqlProcess> findMetadataLockProcesses(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST WHERE State like 'Waiting for Waiting for table metadata lock'";
        List<MysqlProcess> metadataLockProcesses = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");
            try (PreparedStatement stmt = connection.prepareStatement(SQL);
                 ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String user = resultSet.getString("User");
                    String host = resultSet.getString("Host");
                    String db = resultSet.getString("db");
                    String command = resultSet.getString("Command");
                    long time = resultSet.getLong("Time");
                    String state = resultSet.getString("State");
                    String info = resultSet.getString("Info");
                    metadataLockProcesses.add(new MysqlProcess(id, user, host, db, command, time, state, info));
                }
                return metadataLockProcesses;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("metadata lock process 정보를 불러올 수 없습니다.");
    }
}
