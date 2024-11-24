package zzangmin.db_automation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.convention.CommonConvention;
import zzangmin.db_automation.entity.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.mysqlobject.*;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class MysqlClient {

    private static final int COMMAND_TIMEOUT_SECONDS = 600;
    private static final int HEALTHCHECK_TIMEOUT_SECONDS = 3;


    public void executeSQL(DatabaseConnectionInfo databaseConnectionInfo, String SQL) {
        log.info("SQL: {}", SQL);
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateWriterConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            log.info("executeSQL: {}", statement);
            statement.setQueryTimeout(COMMAND_TIMEOUT_SECONDS);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> executeSelectQuery(DatabaseConnectionInfo databaseConnectionInfo, String sql) {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            log.info("executeSelectQuery: {}", statement);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    row.put(columnName, columnValue);
                }
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public Map<String, String> findGlobalVariables(DatabaseConnectionInfo databaseConnectionInfo, List<String> variableNames) {
        Map<String, String> globalVariables = new HashMap<>();

        String SQL = "SHOW GLOBAL VARIABLES WHERE Variable_name IN (?)";
        String variableString = "('" + String.join("','", variableNames) + "')";
        SQL += variableString;
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            log.info("findGlobalVariables: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String variableName = resultSet.getString("Variable_name");
                    String value = resultSet.getString("Value");
                    globalVariables.put(variableName, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return globalVariables;
    }

    public List<String> findInstalledPluginsAndComponentNames(DatabaseConnectionInfo databaseConnectionInfo) {
        List<String> installedNames = new ArrayList<>();

        String pluginSQL = "SELECT PLUGIN_NAME FROM INFORMATION_SCHEMA.PLUGINS WHERE PLUGIN_STATUS LIKE 'ACTIVE'";
        String componentSQL = "SELECT component_urn FROM mysql.COMPONENT";

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword())) {

            try (PreparedStatement pluginStatement = connection.prepareStatement(pluginSQL);
                 ResultSet pluginResultSet = pluginStatement.executeQuery()) {
                log.info("findInstalledPluginsAndComponents - Plugins: {}", pluginStatement);
                while (pluginResultSet.next()) {
                    installedNames.add(pluginResultSet.getString("PLUGIN_NAME"));
                }
            }

            try (PreparedStatement componentStatement = connection.prepareStatement(componentSQL);
                 ResultSet componentResultSet = componentStatement.executeQuery()) {
                log.info("findInstalledPluginsAndComponents - Components: {}", componentStatement);
                while (componentResultSet.next()) {
                    installedNames.add(componentResultSet.getString("component_urn"));
                }
            } catch (SQLException e) {
                log.warn("findInstalledPluginsAndComponents - Components table not found: {}", e.getMessage());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return installedNames;
    }

    public List<String> findTableNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT table_name FROM information_schema.tables WHERE TABLE_TYPE !='VIEW' AND table_schema = ?";
        List<String> tableNames = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                    databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());

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

    public Optional<String> findCreateDatabaseStatement(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SHOW CREATE DATABASE `" + schemaName + "`";
        String createDatabaseStatement = "";

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(),
                databaseConnectionInfo.getUsername(),
                databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet rs = statement.executeQuery()) {

            log.info("findCreateDatabaseStatement: {}", statement);

            if (rs.next()) {
                createDatabaseStatement = rs.getString(2);
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
        return Optional.of(createDatabaseStatement);
    }

    public String findCreateTableStatement(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SHOW CREATE TABLE `" + schemaName + "`.`" + tableName + "`";
        String createTableStatement = "";

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(),
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
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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

    public List<Constraint> findIndexes(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        String SQL = "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = '" + schemaName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY INDEX_NAME, SEQ_IN_INDEX";
        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());

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
                        indexTypes.put(indexName, "PRIMARY");
                    } else {
                        indexTypes.put(indexName, "UNIQUE");
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
                Constraint constraint = new Constraint(Constraint.ConstraintType.valueOf(type), String.join("_", columnNames), columnNames);
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
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            log.info("findColumns: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String findColumnName = resultSet.getString("COLUMN_NAME");
                    String type = resultSet.getString("DATA_TYPE");
                    long characterMaxLength = resultSet.getLong("CHARACTER_MAXIMUM_LENGTH");
                    String isNull = resultSet.getString("IS_NULLABLE");
                    String key = resultSet.getString("COLUMN_KEY");
                        String defaultValue = resultSet.getString("COLUMN_DEFAULT");
                    String extra = resultSet.getString("Extra");
                    String columnComment = resultSet.getString("COLUMN_COMMENT");
                    String charset = resultSet.getString("CHARACTER_SET_NAME");
                    String collate = resultSet.getString("COLLATION_NAME");

                    boolean isNullValue = isNull.equals("YES");
//                    boolean isUniqueKey = key.equals("UNI");
                    boolean isAutoIncrement = extra.equals("auto_increment");
                    type = Objects.isNull(characterMaxLength) ? type : type + "(" + characterMaxLength + ")";
                    columns.add(new Column(
                            findColumnName,
                            type,
                            isNullValue,
                            defaultValue,
                            //isUniqueKey,
                            isAutoIncrement,
                            columnComment,
//                            Objects.isNull(charset) ? CommonConvention.CHARSET : charset,
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
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
                    // boolean isUniqueKey = key.equals("UNI");
                    boolean isAutoIncrement = extra.equals("auto_increment");
                    type = Objects.isNull(characterMaxLength) ? type : type + "(" + characterMaxLength + ")";
                    return Optional.of(new Column(
                            findColumnName,
                            type,
                            isNullValue,
                            defaultValue,
                            //isUniqueKey,
                            isAutoIncrement,
                            columnComment,
//                            Objects.isNull(charset) ? null : charset,
                            Objects.isNull(collate) ? null : collate));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return Optional.empty();
    }

    public List<MysqlAccount> findMysqlAccounts(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SELECT USER,HOST FROM mysql.user";
        List<MysqlAccount> mysqlAccounts = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String user = resultSet.getString("User");
                String host = resultSet.getString("Host");
                MysqlAccount mysqlAccount = MysqlAccount.builder()
                        .serviceName(databaseConnectionInfo.getServiceName())
                        .user(user)
                        .host(host)
                        .build();
                List<MysqlAccount.Privilege> privilegesForUser = findPrivilegesForUser(mysqlAccount, connection);
                mysqlAccount.setPrivileges(privilegesForUser);
                mysqlAccounts.add(mysqlAccount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mysqlAccounts;
    }

    public List<String> findPrivilegeString(DatabaseConnectionInfo databaseConnectionInfo, String accountName) {
        List<String> privileges = new ArrayList<>();
        String showGrantsQuery = "SHOW GRANTS FOR " + accountName;
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(showGrantsQuery)) {
            log.info("findPrivilegeString: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String privilegeString = resultSet.getString(1);
                    privileges.add(privilegeString);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return privileges;
    }

    private List<MysqlAccount.Privilege> findPrivilegesForUser(MysqlAccount mysqlAccount, Connection connection) throws SQLException {
        List<MysqlAccount.Privilege> privileges = new ArrayList<>();
        String showGrantsQuery = "SHOW GRANTS FOR '" + mysqlAccount.getUser() + "'@'" + mysqlAccount.getHost() + "'";
        try (PreparedStatement statement = connection.prepareStatement(showGrantsQuery)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String privilegeString = resultSet.getString(1);
                    if (privilegeString.contains(" ON ")) {
                        List<MysqlAccount.Privilege> generatePrivilege = MysqlAccount.Privilege.dclToEntities(privilegeString);
                        for (MysqlAccount.Privilege privilege : generatePrivilege) {
                            privilege.setMysqlAccount(mysqlAccount);
                        }
                        privileges.addAll(generatePrivilege);
                    }
                }
            }
        }
        return privileges;
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

        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
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
        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.generateReadOnlyConnectionUrl(),
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
        try (Connection connection = DriverManager.getConnection(databaseConnectionInfo.generateReadOnlyConnectionUrl(),
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

    public List<String> findFunctionNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT routine_name FROM information_schema.routines WHERE routine_type = 'FUNCTION' AND routine_schema = ?";
        List<String> functionNames = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            log.info("findFunctionNames: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    functionNames.add(resultSet.getString("routine_name"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return functionNames;
    }

    public List<Function> findFunctions(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT routine_name, " +
                "data_type, " +
                "character_set_name, " +
                "collation_name, " +
                "routine_definition, " +
                "is_deterministic, " +
                "definer, " +
                "character_set_client, " +
                "collation_connection, " +
                "database_collation, " +
                "security_type " +
                "FROM information_schema.routines " +
                "WHERE routine_type = 'FUNCTION' " +
                "AND routine_schema = ?";
        List<Function> functions = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            log.info("findFunctions: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    functions.add(
                    Function.builder()
                            .functionName(resultSet.getString("routine_name"))
                            .dataType(resultSet.getString("data_type"))
                            .characterSetName(resultSet.getString("character_set_name"))
                            .collationName(resultSet.getString("collation_name"))
                            .routineDefinition(resultSet.getString("routine_definition").trim())
                            .isDeterministic(resultSet.getString("routine_name") == "NO" ? false : true)
                            .definer(Definer.splitDefiner(resultSet.getString("definer")))
                            .characterSetClient(resultSet.getString("character_set_client"))
                            .collationConnection(resultSet.getString("collation_connection"))
                            .collationConnection(resultSet.getString("database_collation"))
                            .securityType(resultSet.getString("security_type"))
                            .build()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return functions;
    }

    public List<Procedure> findProcedures(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT routine_name, " +
                "data_type, " +
                "character_set_name, " +
                "collation_name, " +
                "routine_definition, " +
                "is_deterministic, " +
                "definer, " +
                "character_set_client, " +
                "collation_connection, " +
                "database_collation, " +
                "security_type " +
                "FROM information_schema.routines " +
                "WHERE routine_type = 'PROCEDURE' " +
                "AND routine_schema = ?";
        List<Procedure> procedures = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            log.info("findProcedures: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    procedures.add(
                            Procedure.builder()
                                    .procedureName(resultSet.getString("routine_name"))
                                    .dataType(resultSet.getString("data_type"))
                                    .characterSetName(resultSet.getString("character_set_name"))
                                    .collationName(resultSet.getString("collation_name"))
                                    .routineDefinition(resultSet.getString("routine_definition").trim())
                                    .isDeterministic(resultSet.getString("routine_name") == "NO" ? false : true)
                                    .definer(Definer.splitDefiner(resultSet.getString("definer")))
                                    .characterSetClient(resultSet.getString("character_set_client"))
                                    .collationConnection(resultSet.getString("collation_connection"))
                                    .collationConnection(resultSet.getString("database_collation"))
                                    .securityType(resultSet.getString("security_type"))
                                    .build()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return procedures;
    }

    public List<Trigger> findTriggers(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT TRIGGERS.TRIGGER_NAME, " +
                "TRIGGERS.EVENT_MANIPULATION, " +
                "TRIGGERS.EVENT_OBJECT_TABLE, " +
                "TRIGGERS.ACTION_ORDER, " +
                "TRIGGERS.ACTION_STATEMENT, " +
                "TRIGGERS.ACTION_ORIENTATION, " +
                "TRIGGERS.DEFINER, " +
                "TRIGGERS.CHARACTER_SET_CLIENT, " +
                "TRIGGERS.COLLATION_CONNECTION, " +
                "TRIGGERS.DATABASE_COLLATION " +
                "FROM information_schema.TRIGGERS " +
                "WHERE TRIGGERS.TRIGGER_SCHEMA = ?";
        List<Trigger> triggers = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            log.info("findTriggers: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    triggers.add(
                            Trigger.builder()
                                    .triggerName(resultSet.getString("TRIGGER_NAME"))
                                    .eventManipulation(resultSet.getString("EVENT_MANIPULATION"))
                                    .eventObjectTable(resultSet.getString("EVENT_OBJECT_TABLE"))
                                    .actionOrder(resultSet.getInt("ACTION_ORDER"))
                                    .actionStatement(resultSet.getString("ACTION_STATEMENT").trim())
                                    .actionOrientation(resultSet.getString("ACTION_ORIENTATION"))
                                    .definer(Definer.splitDefiner(resultSet.getString("DEFINER")))
                                    .characterSetClient(resultSet.getString("character_set_client"))
                                    .collationConnection(resultSet.getString("collation_connection"))
                                    .collationConnection(resultSet.getString("database_collation"))
                                    .build()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return triggers;
    }

    public List<View> findViews(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        String SQL = "SELECT VIEWS.TABLE_NAME, " +
                "VIEWS.VIEW_DEFINITION, " +
                "VIEWS.DEFINER, " +
                "VIEWS.SECURITY_TYPE, " +
                "VIEWS.CHARACTER_SET_CLIENT, " +
                "VIEWS.COLLATION_CONNECTION " +
                "FROM information_schema.VIEWS " +
                "WHERE VIEWS.TABLE_SCHEMA = ?";
        List<View> views = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, schemaName);
            log.info("findViews: {}", statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    views.add(
                            View.builder()
                                    .viewName(resultSet.getString("TABLE_NAME"))
                                    .viewDefinition(resultSet.getString("VIEW_DEFINITION").trim())
                                    .definer(Definer.splitDefiner(resultSet.getString("DEFINER")))
                                    .securityType(resultSet.getString("SECURITY_TYPE"))
                                    .characterSetClient(resultSet.getString("character_set_client"))
                                    .collationConnection(resultSet.getString("collation_connection"))
                                    .build()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return views;
    }


    public List<Table> findTables(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, List<String> tableNames) {
        String findTableAndColumnSQL = "SELECT t.TABLE_NAME, t.TABLE_SCHEMA, t.TABLE_TYPE, t.ENGINE, t.CREATE_TIME, t.UPDATE_TIME, t.TABLE_COLLATION, t.TABLE_COMMENT, " +
                "c.COLUMN_NAME, c.DATA_TYPE, c.CHARACTER_MAXIMUM_LENGTH, c.IS_NULLABLE, c.COLUMN_KEY, " +
                "c.COLUMN_DEFAULT, c.Extra, c.COLUMN_COMMENT, c.CHARACTER_SET_NAME as column_charset, c.COLLATION_NAME, CCSA.CHARACTER_SET_NAME as table_charset " +
                "FROM INFORMATION_SCHEMA.TABLES t " +
                "INNER JOIN INFORMATION_SCHEMA.COLLATION_CHARACTER_SET_APPLICABILITY CCSA ON CCSA.COLLATION_NAME = t.TABLE_COLLATION " +
                "JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME " +
                "WHERE t.TABLE_SCHEMA = ? AND TABLE_TYPE != 'VIEW' AND t.TABLE_NAME IN ";
        String findIndexSQL = "SELECT INDEX_NAME, COLUMN_NAME, TABLE_NAME, NON_UNIQUE " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME IN ";

        Map<String, List<Column>> tableColumns = new HashMap<>();
        Map<String, Table> tables = new HashMap<>();

        String tableNamesStr = "('" + String.join("','", tableNames) + "')";
        findTableAndColumnSQL += tableNamesStr;
        findIndexSQL += tableNamesStr;

        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(findTableAndColumnSQL)) {

            statement.setString(1, schemaName);

            log.info("findTables: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    String tableEngine = resultSet.getString("ENGINE");
                    String tableCharset = resultSet.getString("table_charset");
                    String tableCollate = resultSet.getString("TABLE_COLLATION");
                    String tableComment = resultSet.getString("TABLE_COMMENT");

                    String columnName = resultSet.getString("COLUMN_NAME");
                    String columnType = resultSet.getString("DATA_TYPE");
                    long characterMaxLength = resultSet.getLong("CHARACTER_MAXIMUM_LENGTH");
                    String isNull = resultSet.getString("IS_NULLABLE");
                    String key = resultSet.getString("COLUMN_KEY");
                    String defaultValue = resultSet.getString("COLUMN_DEFAULT");
                    String extra = resultSet.getString("Extra");
                    String columnComment = resultSet.getString("COLUMN_COMMENT");
                    String columnCharset = resultSet.getString("column_charset");
                    String columnCollate = resultSet.getString("COLLATION_NAME");

                    boolean isNullValue = isNull.equals("YES");
                    //boolean isUniqueKey = key.equals("UNI");
                    boolean isAutoIncrement = extra.equals("auto_increment");
                    columnType = Objects.isNull(characterMaxLength) ? columnType : columnType + "(" + characterMaxLength + ")";
                    Column column = new Column(
                            columnName,
                            columnType,
                            isNullValue,
                            defaultValue,
                            //isUniqueKey,
                            isAutoIncrement,
                            columnComment,
//                            Objects.isNull(columnCharset) ? null : columnCharset,
                            Objects.isNull(columnCollate) ? null : columnCollate);
                    Table table = Table.builder()
                            .tableName(tableName)
                            .tableEngine(tableEngine)
                            .columns(new LinkedHashSet<>())
                            .constraints(new LinkedHashSet<>())
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

                    Constraint constraint = new Constraint(indexName.equals("PRIMARY") ? Constraint.ConstraintType.PRIMARY : (nonUnique.equals("1") ? Constraint.ConstraintType.KEY : Constraint.ConstraintType.UNIQUE), indexName,  new ArrayList<>());

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
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return tables.values()
                .stream()
                .toList();
    }

    public int findLongTransactionProcesslistId(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SELECT PROCESSLIST_ID FROM performance_schema.threads " +
                "WHERE PROCESSLIST_ID IS NOT NULL AND TYPE='FOREGROUND' AND PROCESSLIST_USER!='rdsadmin' AND PROCESSLIST_USER!='event_scheduler' " +
                "ORDER BY PROCESSLIST_TIME DESC limit 1;";
        int processlistId = -1;
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL)) {
            log.info("findLongTransactionProcesslistId: {}", SQL);
            if (resultSet.next()) {
                processlistId = resultSet.getInt("PROCESSLIST_ID");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (processlistId == -1L) {
            throw new IllegalStateException("PROCSSLIST_ID 를 불러오는 데 실패했습니다. " + databaseConnectionInfo);
        }
        return processlistId;
    }

    public List<Query> findQueryInTransaction(DatabaseConnectionInfo databaseConnectionInfo, int processlistId) {
        String SQL = "SELECT ps.id , " +
                "       ps.user, " +
                "       ps.host, " +
                "       esh.event_name, " +
                "       esh.sql_text , " +
                "       esh.digest_text " +
                "FROM information_schema.innodb_trx trx " +
                "JOIN information_schema.processlist ps ON trx.trx_mysql_thread_id = ps.id " +
                "JOIN performance_schema.threads th ON th.processlist_id = trx.trx_mysql_thread_id " +
                "AND trx.trx_mysql_thread_id = ? " +
                "JOIN performance_schema.events_statements_history esh ON esh.thread_id = th.thread_id " +
                "WHERE esh.EVENT_ID >= ( " +
                "    SELECT MAX(EVENT_ID) " +
                "    FROM performance_schema.events_statements_history " +
                "    WHERE EVENT_NAME = 'statement/sql/begin') " +
                "ORDER BY esh.EVENT_ID";


        List<Query> queries = new ArrayList();
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            statement.setString(1, String.valueOf(processlistId));
            log.info("findQueryInTransaction: {}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int findProcesslistId = resultSet.getInt("id");
                    String user = resultSet.getString("user");
                    String host = resultSet.getString("host");
                    String eventName = resultSet.getString("event_name");
                    String sqlText = resultSet.getString("sql_text");
                    String digestText = resultSet.getString("digest_text");
                    queries.add(new Query(databaseConnectionInfo.getDatabaseName(), findProcesslistId, user, host, eventName, sqlText, digestText));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return queries;
    }

    public long findHistoryListLength(DatabaseConnectionInfo databaseConnectionInfo) {
        String SQL = "SELECT COUNT FROM INFORMATION_SCHEMA.INNODB_METRICS WHERE NAME = 'trx_rseg_history_len'";
        long historyListLength = -1L;
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL)) {
            log.info("findHistoryListLength: {}", SQL);
            if (resultSet.next()) {
                historyListLength = resultSet.getLong("COUNT");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (historyListLength == -1L) {
            throw new IllegalStateException("historyListLength 를 불러오는 데 실패했습니다. " + databaseConnectionInfo);
        }
        return historyListLength;
    }

    public void healthCheck(DatabaseConnectionInfo databaseConnectionInfo) {
        log.info("health check start: {}", databaseConnectionInfo.getDatabaseName());
        String SQL = "SELECT 1 FROM DUAL";
        try (Connection connection = DriverManager.getConnection(
                databaseConnectionInfo.generateReadOnlyConnectionUrl(), databaseConnectionInfo.getUsername(), databaseConnectionInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            log.info("healthCheck: {}", statement);
            statement.setQueryTimeout(HEALTHCHECK_TIMEOUT_SECONDS);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            log.info("health check failed: {}", databaseConnectionInfo.getDatabaseName());
            throw new IllegalStateException("헬스 체크에 실패했습니다. 연결을 재확인하세요.\nDatabase: " + databaseConnectionInfo);
        }
        log.info("health check finish: {}", databaseConnectionInfo.getDatabaseName());
    }

}
