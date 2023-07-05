package zzangmin.db_automation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.convention.CommonConvention;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.MetadataLockHolder;
import zzangmin.db_automation.entity.MysqlProcess;
import zzangmin.db_automation.entity.TableStatus;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class MysqlClient {

    private static final int COMMAND_TIMEOUT_SECONDS = 600;

    public void executeSQL(DatabaseConnectionInfo databaseConnectionInfo, String SQL) {
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(COMMAND_TIMEOUT_SECONDS);
            statement.execute(SQL);
            log.info("executeSQL: {}", statement);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Set<String> findTableNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
        Set<String> tableNames = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            log.info("findTableNames: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString("table_name"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return tableNames;
    }

    public Set<String> findSchemaNames(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SHOW DATABASES";
        Set<String> schemaNames = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL)) {
            log.info("findSchemaNames: {}", SQL);
            while (resultSet.next()) {
                schemaNames.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");

            Statement statement = connection.createStatement();
            log.info("findLongQueries: {}", statement);
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
            throw new RuntimeException(e.getMessage());
        }
        return longQueries;
    }

    public String findCreateTableStatement(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SHOW CREATE TABLE `" + schemaName + "`.`" + tableName + "`";
        String createTableStatement = "";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
            try (PreparedStatement statement = connection.prepareStatement(SQL);
                 ResultSet rs = statement.executeQuery()) {
                log.info("findCreateTableStatement: {}", statement);
                if (rs.next()) {
                    createTableStatement = rs.getString(2);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return createTableStatement;
    }

    public TableStatus findTableStatus(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT TABLE_NAME, TABLE_SCHEMA, TABLE_TYPE, ENGINE, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, CREATE_TIME, UPDATE_TIME " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
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
                    log.info("findTableStatus: {}", statement);
                    return new TableStatus(schema, table, type, engine, rows, dataLength, indexLength, createTime, updateTime);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        throw new IllegalStateException("테이블 정보를 불러올 수 없습니다.");
    }

    public List<TableStatus> findTableStatuses(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, Set<String> tableNames) {
        String SQL = "SELECT TABLE_NAME, TABLE_SCHEMA, TABLE_TYPE, ENGINE, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, CREATE_TIME, UPDATE_TIME " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME IN ";
        String tableNamesStr = "('" + String.join("','", tableNames) + "')";
        SQL += tableNamesStr;

        List<TableStatus> tableStatuses = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            statement.setString(1, schemaName);
            log.info("findTableStatuses: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String schema = resultSet.getString("TABLE_SCHEMA");
                    String table = resultSet.getString("TABLE_NAME");
                    String type = resultSet.getString("TABLE_TYPE");
                    String engine = resultSet.getString("ENGINE");
                    int rows = resultSet.getInt("TABLE_ROWS");
                    long dataLength = resultSet.getLong("DATA_LENGTH");
                    long indexLength = resultSet.getLong("INDEX_LENGTH");
                    String createTime = resultSet.getString("CREATE_TIME");
                    String updateTime = resultSet.getString("UPDATE_TIME");

                    TableStatus tableStatus = new TableStatus(schema, table, type, engine, rows, dataLength, indexLength, createTime, updateTime);
                    tableStatuses.add(tableStatus);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return tableStatuses;
    }

    public Map<String, List<String>> findIndexes(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT INDEX_NAME, COLUMN_NAME " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = '" + schemaName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY INDEX_NAME, SEQ_IN_INDEX";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");

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
            log.info("findTableStatus: {}", statement);
            return constraints;
        } catch (SQLException e) {
            throw new IllegalStateException("인덱스 정보를 불러올 수 없습니다.");
        }
    }

    public List<Column> findColumns(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT * " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        List<Column> columns = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            log.info("findColumns: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String findColumnName = resultSet.getString("COLUMN_NAME");
                    String type = resultSet.getString("DATA_TYPE");
                    int characterMaxLength = resultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
                    String isNull = resultSet.getString("IS_NULLABLE");
                    String key = resultSet.getString("COLUMN_KEY");
                    String defaultValue = resultSet.getString("COLUMN_DEFAULT");
                    String extra = resultSet.getString("Extra");
                    String columnComment = resultSet.getString("COLUMN_COMMENT");
                    String charset = resultSet.getString("CHARACTER_SET_NAME");
                    String collate = resultSet.getString("COLLATION_NAME");

                    boolean isNullValue = isNull.equals("YES");
                    boolean isUniqueKey = key.equals("UNI");
                    boolean isAutoIncrement = extra.equals("auto_increment");
                    type = Objects.isNull(characterMaxLength) ? type : type + "(" + characterMaxLength + ")";
                    columns.add(new Column(
                            findColumnName,
                            type,
                            isNullValue,
                            defaultValue,
                            isUniqueKey,
                            isAutoIncrement,
                            columnComment,
                            Objects.isNull(charset) ? CommonConvention.CHARSET : charset,
                            Objects.isNull(collate) ? CommonConvention.COLLATE : collate));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return columns;
    }

    public Optional<Column> findColumn(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName, String columnName) {
        String SQL = "SELECT * " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            statement.setString(3, columnName);
            log.info("findColumn: {}", statement);


            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String findColumnName = resultSet.getString("COLUMN_NAME");
                    String type = resultSet.getString("DATA_TYPE");
                    int characterMaxLength = resultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
                    String isNull = resultSet.getString("IS_NULLABLE");
                    String key = resultSet.getString("COLUMN_KEY");
                    String defaultValue = resultSet.getString("COLUMN_DEFAULT");
                    String extra = resultSet.getString("Extra");
                    String columnComment = resultSet.getString("COLUMN_COMMENT");
                    String charset = resultSet.getString("CHARACTER_SET_NAME");
                    String collate = resultSet.getString("COLLATION_NAME");

                    boolean isNullValue = isNull.equals("YES");
                    boolean isUniqueKey = key.equals("UNI");
                    boolean isAutoIncrement = extra.equals("auto_increment");
                    type = Objects.isNull(characterMaxLength) ? type : type + "(" + characterMaxLength + ")";
                    return Optional.of(new Column(
                            findColumnName,
                            type,
                            isNullValue,
                            defaultValue,
                            isUniqueKey,
                            isAutoIncrement,
                            columnComment,
                            Objects.isNull(charset) ? CommonConvention.CHARSET : charset,
                            Objects.isNull(collate) ? CommonConvention.COLLATE : collate));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return Optional.empty();
    }



    public List<MetadataLockHolder> findMetadataLockHolders(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SELECT " +
                "MLOCK2.OBJECT_TYPE, " +
                "MLOCK2.OBJECT_SCHEMA, " +
                "MLOCK2.OBJECT_NAME, " +
                "MLOCK2.LOCK_TYPE, " +
                "MLOCK2.LOCK_STATUS, " +
                "THREADS.THREAD_ID, " +
                "THREADS.PROCESSLIST_ID, " +
                "THREADS.PROCESSLIST_INFO, " +
                "THREADS.PROCESSLIST_TIME " +
                "FROM " +
                "performance_schema.metadata_locks MLOCK1 " +
                "JOIN " +
                "performance_schema.metadata_locks MLOCK2 " +
                "ON " +
                "MLOCK1.OWNER_THREAD_ID <> MLOCK2.OWNER_THREAD_ID " +
                "AND " +
                "MLOCK1.OBJECT_NAME = MLOCK2.OBJECT_NAME " +
                "AND " +
                "MLOCK1.LOCK_STATUS = 'PENDING' " +
                "JOIN " +
                "performance_schema.threads THREADS " +
                "ON " +
                "MLOCK2.OWNER_THREAD_ID = THREADS.THREAD_ID";

        List<MetadataLockHolder> metadataLockHolders = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String objectType = resultSet.getString("OBJECT_TYPE");
                String objectSchema = resultSet.getString("OBJECT_SCHEMA");
                String objectName = resultSet.getString("OBJECT_NAME");
                String lockType = resultSet.getString("LOCK_TYPE");
                String lockStatus = resultSet.getString("LOCK_STATUS");
                long threadId = resultSet.getLong("THREAD_ID");
                long processlistId = resultSet.getLong("PROCESSLIST_ID");
                String processlistInfo = resultSet.getString("PROCESSLIST_INFO");
                long processlistTime = resultSet.getLong("PROCESSLIST_TIME");

                metadataLockHolders.add(new MetadataLockHolder(objectType, objectSchema, objectName, lockType, lockStatus, threadId, processlistId, processlistInfo, processlistTime));
            }
            log.info("findMetadataLockHolders: {}", statement);
            return metadataLockHolders;
        } catch (SQLException e) {
            throw new IllegalStateException("metadata lock holder process 정보를 불러올 수 없습니다.");
        }
    }

    public void killSession(DatabaseConnectionInfo databaseConnectionInfo, long sessionId) {
        String SQL = "KILL " + sessionId;
        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.getUrl(),
                databaseConnectionInfo.getUsername(), "mysql5128*");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(SQL);
            log.info("killSession: {}", statement);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Optional<MysqlProcess> findDDLExecutingSession(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST WHERE COMMAND = 'Query' AND INFO LIKE 'ALTER%' OR INFO LIKE 'CREATE%' OR INFO LIKE 'DROP%'";
        Optional<MysqlProcess> mysqlProcesses = Optional.empty();
        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.getUrl(),
                databaseConnectionInfo.getUsername(), "mysql5128*");
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                long id = resultSet.getLong("ID");
                String user = resultSet.getString("USER");
                String host = resultSet.getString("HOST");
                String db = resultSet.getString("DB");
                String command = resultSet.getString("COMMAND");
                long time = resultSet.getLong("TIME");
                String state = resultSet.getString("STATE");
                String info = resultSet.getString("INFO");
                mysqlProcesses = Optional.ofNullable(new MysqlProcess(id, user, host, db, command, time, state, info));
            }
            log.info("findDDLExecutingSession: {}", statement);
            return mysqlProcesses;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
