package zzangmin.db_automation.client;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.testfactory.DatabaseConnectionInfoFactory;
import zzangmin.db_automation.entity.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MysqlClientTest {

    @Autowired
    private DatabaseConnectionInfoFactory databaseConnectionInfoFactory;

    @Autowired
    private MysqlClient mysqlClient;
    @Autowired
    private AwsService awsService;

    private DatabaseConnectionInfo backOfficeDatabaseConnectionInfo;
    private String schemaName = "test_schema";

    @BeforeEach
    public void setUp() {
        backOfficeDatabaseConnectionInfo = databaseConnectionInfoFactory.createDatabaseConnectionInfo();
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TRIGGER IF EXISTS test_schema.t1");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP VIEW IF EXISTS test_schema.v1");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP FUNCTION IF EXISTS test_schema.f1");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP PROCEDURE IF EXISTS test_schema.p1");

        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE TABLE test_schema.test_table (id INT NOT NULL AUTO_INCREMENT COMMENT 'asdf', name VARCHAR(45) NULL COMMENT 'name comment', PRIMARY KEY (id), KEY name(name)) COMMENT 'TABLE COMMENT'");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "INSERT INTO test_schema.test_table (name) VALUES ('test_name')");

        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE FUNCTION test_schema.f1(p_price DECIMAL(10,2)) " +
                        "RETURNS DECIMAL(10,2) " +
                        "BEGIN " +
                        "   RETURN p_price * 1.11; " +
                        "END");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE PROCEDURE test_schema.p1(IN p_name VARCHAR(255), IN p_email VARCHAR(255))\n" +
                "BEGIN " +
                "    INSERT INTO customers (name, email) VALUES (p_name, p_email); " +
                "END");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE TRIGGER test_schema.t1 AFTER INSERT ON test_table " +
                "    FOR EACH ROW " +
                "BEGIN " +
                "    UPDATE test_schema.test_table SET name = '1' WHERE name = '1'; " +
                "END");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE VIEW test_schema.v1 AS " +
                "SELECT id " +
                "FROM test_schema.test_table " +
                "LIMIT 1;");
    }

    @AfterEach
    public void tearDown() {
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TRIGGER IF EXISTS test_schema.t1");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP VIEW IF EXISTS test_schema.v1");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP FUNCTION IF EXISTS test_schema.f1");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP PROCEDURE IF EXISTS test_schema.p1");
    }

    @DisplayName("executeSQL 을 통해 SQL 을 실행할 수 있다.")
    @Test
    public void testExecuteSQL() {
        // given
        String sql = "CREATE TABLE test_schema.test_table2 (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(45) NULL, PRIMARY KEY (id))";

        // when
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, sql);

        // then
        try (Connection connection = DriverManager.getConnection(
                backOfficeDatabaseConnectionInfo.getUrl(), backOfficeDatabaseConnectionInfo.getUsername(), backOfficeDatabaseConnectionInfo.getPassword());
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) from test_schema.test_table");
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                assertEquals(1, count);
            }
        } catch (SQLException e) {
            fail(e.getMessage());
        }

        // cleanup
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE test_schema.test_table2");
    }

    @DisplayName("findTableNames 로 특정 스키마의 테이블 이름을 조회할 수 있다.")
    @Test
    public void testFindTableNames() {
        // given

        // when
        List<String> tableNames = mysqlClient.findTableNames(backOfficeDatabaseConnectionInfo, schemaName);

        // then
        assertThat(tableNames.contains("test_table")).isTrue();

    }

    @DisplayName("findSchemaNames로 데이터베이스의 스키마 이름을 조회할 수 있다.")
    @Test
    public void testFindSchemaNames() {
        // Given

        // When
        List<String> schemaNames = mysqlClient.findSchemaNames(backOfficeDatabaseConnectionInfo);

        // Then
        assertTrue(schemaNames.contains("test_schema"));
    }

    @DisplayName("findLongQueries로 지정된 시간 이상의 쿼리를 조회할 수 있다.")
    @Test
    public void testFindLongQueries() throws Exception {
        // Given (새 스레드 생성해서 롱쿼리 실행)

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> longQueryFuture = executorService.submit(() -> {
            try (Connection connection = DriverManager.getConnection(
                    backOfficeDatabaseConnectionInfo.getUrl(), backOfficeDatabaseConnectionInfo.getUsername(), backOfficeDatabaseConnectionInfo.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.execute("SELECT SLEEP(5)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        int longQueryStandard = 1;
        Thread.sleep(2000);

        // When
        List<MysqlProcess> longQueries = mysqlClient.findLongQueries(backOfficeDatabaseConnectionInfo, longQueryStandard);

        // Then
        assertThat(longQueries.isEmpty()).isFalse();
        for (MysqlProcess process : longQueries) {
            assertTrue(process.getTime() >= longQueryStandard);
        }
    }

    @DisplayName("findCreateTableStatement 메서드로 CREATE TABLE 문을 조회할 수 있다.")
    @Test
    public void testFindCreateTableStatement() {
        // given
        String tableName = "test_table";

        // when
        String createTableStatement = mysqlClient.findCreateTableStatement(backOfficeDatabaseConnectionInfo, schemaName, tableName);

        // then
        assertNotNull(createTableStatement);
        assertTrue(createTableStatement.startsWith("CREATE TABLE"));
        assertTrue(createTableStatement.contains(tableName));

    }

    @DisplayName("findCreateDatabaseStatement으로 database 생성문을 조회할 수 있다.")
    @Test
    void testFindCreateDatabaseStatement() {
        // given
        String schemaName = "test_schema";

        //when
        Optional<String> createDatabaseStatementOptional = mysqlClient.findCreateDatabaseStatement(backOfficeDatabaseConnectionInfo, schemaName);

        //then
        String createDatabaseStatement = createDatabaseStatementOptional.get();
        assertNotNull(createDatabaseStatement);
        assertTrue(createDatabaseStatement.startsWith("CREATE DATABASE"));
        assertTrue(createDatabaseStatement.contains(schemaName));
    }

    @DisplayName("findCreateDatabaseStatement 으로 존재하지 않는 database를 조회하면 Optional empty를 반환한다")
    @Test
    void testOptionalFindCreateDatabaseStatement() {
        // given
        String schemaName = "no_exists_database";

        //when
        Optional<String> createDatabaseStatementOptional = mysqlClient.findCreateDatabaseStatement(backOfficeDatabaseConnectionInfo, schemaName);

        //then

        assertThat(createDatabaseStatementOptional).isEqualTo(Optional.empty());
    }


    @DisplayName("findTableStatus 메서드로 테이블 상태를 조회할 수 있다.")
    @Test
    public void testFindTableStatus() {
        // given
        String tableName = "test_table";

        // when
        TableStatus tableStatus = mysqlClient.findTableStatus(backOfficeDatabaseConnectionInfo, schemaName, tableName);

        // then
        assertNotNull(tableStatus);
        assertEquals(schemaName, tableStatus.getSchemaName());
        assertEquals(tableName, tableStatus.getTableName());
        assertNotNull(tableStatus.getTableType());
        assertNotNull(tableStatus.getTableEngine());
        assertTrue(tableStatus.getTableRow() >= 0);
        assertTrue(tableStatus.getDataLength() >= 0);
        assertTrue(tableStatus.getIndexLength() >= 0);
        assertNotNull(tableStatus.getCreateTime());
    }

    @DisplayName("findIndexes로 테이블의 인덱스 정보를 조회할 수 있다.")
    @Test
    public void testFindIndexes() {
        // given
        String tableName = "test_table";

        // when
        List<Constraint> indexes = mysqlClient.findIndexes(backOfficeDatabaseConnectionInfo, schemaName, tableName);

        // then
        assertNotNull(indexes);
        for (Constraint index : indexes) {
            if (index.getType().equals("PRIMARY KEY")) {
                assertThat(index.getKeyName()).isEqualTo("id");
            } else if (index.getType().equals("KEY")) {
                assertThat(index.getKeyName()).isEqualTo("name");
            }
        }
    }

    @DisplayName("findColumn으로 특정 컬럼 정보를 조회할 수 있다.")
    @Test
    public void testFindColumn() {
        // given
        String tableName = "test_table";
        String columnName = "name";

        // when
        Optional<Column> columnOptional = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, tableName, columnName);

        // then
        assertTrue(columnOptional.isPresent());
        Column column = columnOptional.get();
        assertEquals(columnName, column.getName());
        assertEquals("varchar(45)", column.getType());
        assertTrue(column.getIsNull());
        assertFalse(column.getIsUnique());
        assertFalse(column.getIsAutoIncrement());
        assertNull(column.getDefaultValue());
        assertEquals("name comment", column.getComment());
        assertEquals("utf8mb4", column.getCharset());
        assertEquals("utf8mb4_0900_ai_ci", column.getCollate());
    }

    @DisplayName("findMetadataLockHolders로 Metadata Lock 유발 프로세스를 조회할 수 있다. (performance_schema 활성화 필요)")
    @Test
    public void testFindMetadataLockHolders() throws Exception {
        // given
        String metadataHolderSQL = "select * from test_schema.test_table where sleep(5) = 0 limit 1";
        String metadataWaitSQL = "ALTER TABLE test_schema.test_table ADD INDEX test_index(id, name)";

        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        ExecutorService executorService2 = Executors.newSingleThreadExecutor();

        Future<?> holderFuture = executorService1.submit(() -> {
            mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, metadataHolderSQL);
        });

        Future<?> alterFuture = executorService2.submit(() -> {
            mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, metadataWaitSQL);
        });
        Thread.sleep(1000);

        // when
        List<MetadataLockHolder> metadataLockHolders = mysqlClient.findMetadataLockHolders(backOfficeDatabaseConnectionInfo);
        // then
        assertThat(metadataLockHolders.isEmpty()).isFalse();
        MetadataLockHolder metadataLockHolder = metadataLockHolders.get(0);
        assertThat(metadataLockHolder.getProcessListInfo()).isEqualTo(metadataHolderSQL);
    }

    @DisplayName("killSession으로 세션을 종료할 수 있다.")
    @Test
    public void testKillSession() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> longQueryFuture = executorService.submit(() -> {
            try (Connection connection = DriverManager.getConnection(backOfficeDatabaseConnectionInfo.getUrl(), backOfficeDatabaseConnectionInfo.getUsername(), backOfficeDatabaseConnectionInfo.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.execute("select * from test_schema.test_table where sleep(5)=0 limit 1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(1000);
        MysqlProcess longQuery = mysqlClient.findLongQueries(backOfficeDatabaseConnectionInfo, 1).get(0);
        long longQuerySessionId = longQuery.getId();

        //when
        mysqlClient.killSession(backOfficeDatabaseConnectionInfo, longQuerySessionId);

        //then
        assertThat(mysqlClient.findLongQueries(backOfficeDatabaseConnectionInfo, 1)).isEmpty();
    }

    @DisplayName("findDDLExecutingSession으로 DDL 실행중인 세션을 조회할 수 있다.")
    @Test
    public void testFindDDLExecutingSession() throws Exception {
        // given (Alter 감지를 위해 강제로 metadata lock waiting 유발)
        String metadataHolderSQL = "select * from test_schema.test_table where sleep(5)=0 limit 1";
        String alterSQL = "ALTER TABLE test_schema.test_table ADD INDEX test_index(id, name)";

        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        ExecutorService executorService2 = Executors.newSingleThreadExecutor();

        Future<?> holderFuture = executorService1.submit(() -> {
            try (Connection connection = DriverManager.getConnection(backOfficeDatabaseConnectionInfo.getUrl(), backOfficeDatabaseConnectionInfo.getUsername(), backOfficeDatabaseConnectionInfo.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.execute("start transaction");
                statement.execute(metadataHolderSQL);
                statement.execute("commit ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Future<?> alterFuture = executorService2.submit(() -> {
            try (Connection connection = DriverManager.getConnection(
                    backOfficeDatabaseConnectionInfo.getUrl(), backOfficeDatabaseConnectionInfo.getUsername(), backOfficeDatabaseConnectionInfo.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE test_schema.test_table ADD INDEX test_index(id, name)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(1000);

        // when
        Optional<MysqlProcess> ddlExecutingSession = mysqlClient.findDDLExecutingSession(backOfficeDatabaseConnectionInfo);

        // then
        assertThat(ddlExecutingSession.isPresent()).isTrue();
        assertThat(ddlExecutingSession.get().getInfo()).isEqualTo(alterSQL);

    }


    @DisplayName("findFunctionNames로 mysql function 이름 목록을 조회할 수 있다.")
    @Test
    void findFunctionNames() {
        // given & when
        List<String> functionNames = mysqlClient.findFunctionNames(backOfficeDatabaseConnectionInfo, schemaName);

        // then
        assertThat(functionNames.contains("f1")).isTrue();

    }

    @DisplayName("findFunctions 로 mysql function을 조회할 수 있다.")
    @Test
    void findFunctions() {
        // given & when

        List<Function> functions = mysqlClient.findFunctions(backOfficeDatabaseConnectionInfo, schemaName);

        //then
        assertThat(functions).isNotEmpty();
        assertThat(functions.get(0).getFunctionName()).isEqualTo("f1");
    }

    @DisplayName("findFunctions 로 mysql procedure 를 조회할 수 있다.")
    @Test
    void findProcedures() {
        // given & when

        List<Procedure> procedures = mysqlClient.findProcedures(backOfficeDatabaseConnectionInfo, schemaName);

        //then
        assertThat(procedures).isNotEmpty();
        assertThat(procedures.get(0).getProcedureName()).isEqualTo("p1");
    }

    @DisplayName("findFunctions 로 mysql trigger 를 조회할 수 있다.")
    @Test
    void findTriggers() {
        // given & when
        List<Trigger> triggers = mysqlClient.findTriggers(backOfficeDatabaseConnectionInfo, schemaName);

        //then
        assertThat(triggers).isNotEmpty();
        assertThat(triggers.get(0).getTriggerName()).isEqualTo("t1");
    }

    @DisplayName("findViews 로 mysql view 를 조회할 수 있다.")
    @Test
    void findViews() {
        // given & when
        List<View> views = mysqlClient.findViews(backOfficeDatabaseConnectionInfo, schemaName);

        //then
        assertThat(views).isNotEmpty();
        assertThat(views.get(0).getViewName()).isEqualTo("v1");
    }
}
