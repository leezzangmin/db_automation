package zzangmin.db_automation.validator;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.testfactory.DatabaseConnectionInfoFactory;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
class DDLValidatorTest {
    @Autowired
    private DatabaseConnectionInfoFactory databaseConnectionInfoFactory;

    @Autowired
    private DDLValidator ddlValidator;

    @Autowired
    private MysqlClient mysqlClient;
    @Autowired
    private AwsService awsService;

    private DatabaseConnectionInfo backOfficeDatabaseConnectionInfo;
    private String schemaName = "test_schema";

    @BeforeEach
    public void setUp() {
        backOfficeDatabaseConnectionInfo = databaseConnectionInfoFactory.createDatabaseConnectionInfo();
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE TABLE test_schema.test_table (id INT NOT NULL AUTO_INCREMENT COMMENT 'asdf', name VARCHAR(45) NULL COMMENT 'name comment', PRIMARY KEY (id), KEY name(name)) COMMENT 'TABLE COMMENT'");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "INSERT INTO test_schema.test_table (name) VALUES ('test_name')");
    }

    @AfterEach
    public void tearDown() {
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
    }

    @DisplayName("long query가 있으면 validation이 실패해야 한다.")
    @Test
    void validateLongQueryExists() throws Exception {
        //given
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

        Thread.sleep(3500);

        Column column = new Column("new_column", "VARCHAR(255)", false, null, false, "new column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType_old.ADD_COLUMN);

        //when & then
        Assertions.assertThrows(IllegalStateException.class, () -> ddlValidator.validateDDLRequest(backOfficeDatabaseConnectionInfo, addColumnRequestDTO));
    }

    @DisplayName("add column validation이 정상적으로 수행되어야 한다")
    @Test
    void validateAddColumnTest() {
        //given
        Column column = new Column("new_column", "VARCHAR(255)", false, null, false, "new column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType_old.ADD_COLUMN);
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateAddColumn(backOfficeDatabaseConnectionInfo, addColumnRequestDTO));
    }

    @DisplayName("add column 시 auto_increment 컬럼은 추가할 수 없다.")
    @Test
    void validateAddColumnTest_autoIncrement() {
        //given
        Column column = new Column("new_column", "VARCHAR(255)", false, null, true, "new column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType_old.ADD_COLUMN);
        //when & then

        Assertions.assertThrows(IllegalStateException.class, () -> ddlValidator.validateAddColumn(backOfficeDatabaseConnectionInfo, addColumnRequestDTO));
    }

    @DisplayName("alter column validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateAlterColumnTest() {
        //given
        Column column = new Column("name", "VARCHAR(255)", false, null, false, "alter column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AlterColumnRequestDTO alterColumnRequestDTO = new AlterColumnRequestDTO(schemaName, "test_table", "name", column);
        alterColumnRequestDTO.setCommandType(CommandType_old.ALTER_COLUMN);
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateAlterColumn(backOfficeDatabaseConnectionInfo, alterColumnRequestDTO));
    }

    @DisplayName("create index validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateCreateIndexTest() {
        //given
        CreateIndexRequestDTO createIndexRequestDTO = new CreateIndexRequestDTO(schemaName, "test_table", "id_name", "KEY", List.of("id", "name"));
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateCreateIndex(backOfficeDatabaseConnectionInfo, createIndexRequestDTO));
    }

    @DisplayName("이미 존재하는 인덱스를 추가하려고 하면 오류가 발생해야 한다.")
    @Test
    void validateCreateIndexTest_existIndex() {
        //given
        CreateIndexRequestDTO createIndexRequestDTO = new CreateIndexRequestDTO(schemaName, "test_table", "id", "KEY", List.of("id"));
        //when & then
        Assertions.assertThrows(IllegalStateException.class, () -> ddlValidator.validateCreateIndex(backOfficeDatabaseConnectionInfo, createIndexRequestDTO));
    }

    @DisplayName("KEY 혹은 UNIQUE KEY 가 아닌 인덱스를 추가하려고 하면 오류가 발생해야 한다.")
    @Test
    void validateCreateIndexTest_pk() {
        //given
        CreateIndexRequestDTO createIndexRequestDTO1 = new CreateIndexRequestDTO(schemaName, "test_table", "id_name", "PRIMARY KEY", List.of("id","name"));
        CreateIndexRequestDTO createIndexRequestDTO2 = new CreateIndexRequestDTO(schemaName, "test_table", "id_name", "정체불명 KEY", List.of("id","name"));
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> ddlValidator.validateCreateIndex(backOfficeDatabaseConnectionInfo, createIndexRequestDTO1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ddlValidator.validateCreateIndex(backOfficeDatabaseConnectionInfo, createIndexRequestDTO2));
    }

    @DisplayName("create table validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateCreateTableTest() {
        //given
        Column column1 = new Column("id", "INT", false, "0", true, "column1 comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name", "INT", false, "0", false, "column2 comment", "utf8mb4", "utf8mb4_0900_ai_ci");

        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.UNIQUE, "name", List.of("name"));
        CreateTableRequestDTO createTableRequestDTO = new CreateTableRequestDTO(schemaName, "create_table_test", Set.of(column1), Set.of(constraint1, constraint2), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        createTableRequestDTO.setCommandType(CommandType_old.CREATE_TABLE);
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateCreateTable(backOfficeDatabaseConnectionInfo, createTableRequestDTO));
    }

    @DisplayName("delete column validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateDeleteColumnTest() {
        //given
        DeleteColumnRequestDTO deleteColumnRequestDTO = new DeleteColumnRequestDTO(schemaName, "test_table", "name");
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateDeleteColumn(backOfficeDatabaseConnectionInfo, deleteColumnRequestDTO));
    }

    @DisplayName("extend varchar column validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateExtendVarcharColumnTest() {
        //given
        ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO = new ExtendVarcharColumnRequestDTO(schemaName, "test_table", "name", 50);
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateExtendVarchar(backOfficeDatabaseConnectionInfo, extendVarcharColumnRequestDTO));
    }

    @DisplayName("extend varchar column 시 inplace로 처리되지 않으면 오류가 발생해야 한다.")
    @Test
    void validateExtendVarcharColumnTest_notInPlace() {
        //given
        ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO = new ExtendVarcharColumnRequestDTO(schemaName, "test_table", "name", 500);
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> ddlValidator.validateExtendVarchar(backOfficeDatabaseConnectionInfo, extendVarcharColumnRequestDTO));
    }

    @DisplayName("rename column validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateRenameColumnTest() {
        //given
        RenameColumnRequestDTO renameColumnRequestDTO = new RenameColumnRequestDTO(schemaName, "test_table", "name", "new_name");
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateRenameColumn(backOfficeDatabaseConnectionInfo, renameColumnRequestDTO));
    }
}