package zzangmin.db_automation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.convention.CommonConvention;
import zzangmin.db_automation.entity.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class MysqlClient {

    private static final int COMMAND_TIMEOUT_SECONDS = 600;

    public void executeSQL(DatabaseConnectionInfo databaseConnectionInfo, String SQL) {
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            log.info("executeSQL: {}", statement);
            statement.setQueryTimeout(COMMAND_TIMEOUT_SECONDS);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<String> findTableNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
        List<String> tableNames = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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

    public List<String> findSchemaNames(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SHOW DATABASES";
        List<String> schemaNames = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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


    public List<MysqlProcess> findLongQueries(DatabaseConnectionInfo databaseConnectionInfo, int longQueryStandard) {
        String SQL = "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST " +
                "WHERE COMMAND = 'Query' " +
                "AND USER NOT IN ('rdsadmin', 'event_scheduler') " +
                "AND TIME >= " + longQueryStandard;
        List<MysqlProcess> longQueries = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());

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

    public String findCreateDatabaseStatement(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SHOW CREATE DATABASE `" + schemaName + "`";
        String createDatabaseStatement = "";

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(),
                databaseConnectionInfo.getUsername(),
                databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet rs = statement.executeQuery()) {

            log.info("findCreateDatabaseStatement: {}", statement);

            if (rs.next()) {
                createDatabaseStatement = rs.getString(2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return createDatabaseStatement;
    }

    public String findCreateTableStatement(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SHOW CREATE TABLE `" + schemaName + "`.`" + tableName + "`";
        String createTableStatement = "";

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(),
                databaseConnectionInfo.getUsername(),
                databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet rs = statement.executeQuery()) {

            log.info("findCreateTableStatement: {}", statement);

            if (rs.next()) {
                createTableStatement = rs.getString(2);
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
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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

    public List<TableStatus> findTableStatuses(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, List<String> tableNames) {
        String SQL = "SELECT TABLE_NAME, TABLE_SCHEMA, TABLE_TYPE, ENGINE, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, CREATE_TIME, UPDATE_TIME " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME IN ";
        String tableNamesStr = "('" + String.join("','", tableNames) + "')";
        SQL += tableNamesStr;

        List<TableStatus> tableStatuses = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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

//    public Map<String, List<String>> findIndexes2(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
//        String SQL = "SELECT INDEX_NAME, COLUMN_NAME " +
//                "FROM INFORMATION_SCHEMA.STATISTICS " +
//                "WHERE TABLE_SCHEMA = '" + schemaName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY INDEX_NAME, SEQ_IN_INDEX";
//        try {
//            Connection connection = DriverManager.getConnection(
//                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
//
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(SQL);
//
//            Map<String, List<String>> constraints = new HashMap<>();
//            while (resultSet.next()) {
//                String indexName = resultSet.getString("INDEX_NAME");
//                String columnName = resultSet.getString("COLUMN_NAME");
//                if (constraints.containsKey(indexName)) {
//                    List<String> columns = constraints.get(indexName);
//                    columns.add(columnName);
//                    continue;
//                }
//                List<String> columns = new ArrayList<>();
//                columns.add(columnName);
//                constraints.put(indexName, columns);
//            }
//            log.info("findIndexes: {}", statement);
//            return constraints;
//        } catch (SQLException e) {
//            throw new IllegalStateException("인덱스 정보를 불러올 수 없습니다.");
//        }
//    }

    public List<Constraint> findIndexes(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = '" + schemaName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY INDEX_NAME, SEQ_IN_INDEX";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);

            List<Constraint> constraints = new ArrayList<>();
            Map<String, List<String>> indexNames = new HashMap<>();
            Map<String, String> indexTypes = new HashMap<>();
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                String unique = resultSet.getString("NON_UNIQUE");
                if (unique.equals("0")) {
                    if (indexName.equals("PRIMARY")) {
                        indexTypes.put(indexName, "PRIMARY KEY");
                    } else {
                        indexTypes.put(indexName, "UNIQUE KEY");
                    }
                } else {
                    indexTypes.put(indexName, "KEY");
                }

                if (indexNames.containsKey(indexName)) {
                    List<String> columns = indexNames.get(indexName);
                    columns.add(columnName);
                    continue;
                }
                List<String> columns = new ArrayList<>();
                columns.add(columnName);
                indexNames.put(indexName, columns);
            }

            for (String indexName : indexNames.keySet()) {
                List<String> columnNames = indexNames.get(indexName);
                String type = indexTypes.get(indexName);
                Constraint constraint = new Constraint(type, String.join("_", columnNames), columnNames);
                constraints.add(constraint);
            }
            log.info("findIndexes: {}", statement);
            return constraints;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("인덱스 정보를 불러올 수 없습니다.");
        }
    }

    public List<Column> findColumns(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT * " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        List<Column> columns = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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

        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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

    public List<Table> findTables(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, List<String> tableNames) {
        String findTableAndColumnSQL = "SELECT t.TABLE_NAME, t.TABLE_SCHEMA, t.TABLE_TYPE, t.ENGINE, t.CREATE_TIME, t.UPDATE_TIME, t.TABLE_COLLATION, t.TABLE_COMMENT, " +
                "c.COLUMN_NAME, c.DATA_TYPE, c.CHARACTER_MAXIMUM_LENGTH, c.IS_NULLABLE, c.COLUMN_KEY, " +
                "c.COLUMN_DEFAULT, c.Extra, c.COLUMN_COMMENT, c.CHARACTER_SET_NAME, c.COLLATION_NAME " +
                "FROM INFORMATION_SCHEMA.TABLES t " +
                "LEFT JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME " +
                "WHERE t.TABLE_SCHEMA = ? AND t.TABLE_NAME IN ";
        String findIndexSQL = "SELECT INDEX_NAME, COLUMN_NAME, TABLE_NAME, NON_UNIQUE " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME IN ";

        Map<String, List<Column>> tableColumns = new HashMap<>();
        Map<String, Table> tables = new HashMap<>();

        String tableNamesStr = "('" + String.join("','", tableNames) + "')";
        findTableAndColumnSQL += tableNamesStr;
        findIndexSQL += tableNamesStr;

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(findTableAndColumnSQL)) {

            statement.setString(1, schemaName);

            log.info("findTables: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    String tableEngine = resultSet.getString("ENGINE");
                    String tableCharset = "utf8mb4"; // TODO
                    String tableCollate = resultSet.getString("TABLE_COLLATION");
                    String tableComment = resultSet.getString("TABLE_COMMENT");

                    String columnName = resultSet.getString("COLUMN_NAME");
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
                    Column column = new Column(
                            columnName,
                            type,
                            isNullValue,
                            defaultValue,
                            isUniqueKey,
                            isAutoIncrement,
                            columnComment,
                            Objects.isNull(charset) ? CommonConvention.CHARSET : charset,
                            Objects.isNull(collate) ? CommonConvention.COLLATE : collate);
                    Table table = Table.builder()
                            .tableName(tableName)
                            .tableEngine(tableEngine)
                            .columns(new ArrayList<>())
                            .constraints(new ArrayList<>())
                            .tableCharset(tableCharset)
                            .tableCollate(tableCollate)
                            .tableComment(tableComment)
                            .build();
                    if (tableColumns.containsKey(tableName)) {
                        tableColumns.get(tableName).add(column);
                    } else {
                        tableColumns.put(tableName, new ArrayList<>(List.of(column)));
                    }
                    tables.put(tableName, table);
                }
            }

            Map<String,Map<String, List<String>>> keyColumnNames = new HashMap<>();
            Map<String, Map<String, Constraint>> tableConstraints = new HashMap<>();
            try {
                PreparedStatement findIndexStatement = connection.prepareStatement(findIndexSQL);
                findIndexStatement.setString(1, schemaName);
                ResultSet indexResultSet = findIndexStatement.executeQuery();

                while (indexResultSet.next()) {
                    String tableName = indexResultSet.getString("TABLE_NAME");
                    String indexName = indexResultSet.getString("INDEX_NAME");
                    String columnName = indexResultSet.getString("COLUMN_NAME");
                    String nonUnique = indexResultSet.getString("NON_UNIQUE");

                    if (keyColumnNames.containsKey(tableName)) {
                        if (keyColumnNames.get(tableName).containsKey(indexName)) {
                            keyColumnNames.get(tableName).get(indexName).add(columnName);
                        } else {
                            keyColumnNames.get(tableName).put(indexName, new ArrayList<>(List.of(columnName)));
                        }
                    } else {
                        keyColumnNames.put(tableName, new HashMap<>(Map.of(indexName, new ArrayList<>(List.of(columnName)))));
                    }

                    Constraint constraint = new Constraint(indexName.equals("PRIMARY") ? "PRIMARY KEY" : (nonUnique.equals("1") ? "KEY" : "UNIQUE KEY"), indexName,  new ArrayList<>());

                    if (tableConstraints.containsKey(tableName)) {
                        tableConstraints.get(tableName)
                                .put(indexName, constraint);
                    } else {
                        tableConstraints.put(tableName, new HashMap<>(Map.of(indexName, constraint)));
                    }

                }

                for (String tableName : keyColumnNames.keySet()) {
                    Map<String, List<String>> indexColumnNames = keyColumnNames.get(tableName);
                    for (String indexName : indexColumnNames.keySet()) {
                        tableConstraints.get(tableName)
                                .get(indexName)
                                .addKeyColumnNames(indexColumnNames.get(indexName));
                    }
                }


            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage());
            }

            for (String tableName : tables.keySet()) {
                Table table = tables.get(tableName);
                table.addColumns(tableColumns.get(tableName));

                table.addConstraints(
                        tableConstraints.getOrDefault(tableName, new HashMap<>())
                                .values()
                                .stream()
                                .toList());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return tables.values()
                .stream()
                .toList();
    }
}
