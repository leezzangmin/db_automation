package zzangmin.db_automation.validator;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.AddColumnRequestDTO;
import zzangmin.db_automation.dto.request.AlterColumnRequestDTO;
import zzangmin.db_automation.dto.request.CreateIndexRequestDTO;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DDLValidatorTest {
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
        backOfficeDatabaseConnectionInfo = new DatabaseConnectionInfo("zzangmin-db", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://zzangmin-db.codf49uhek24.ap-northeast-2.rds.amazonaws.com", "admin", awsService.findRdsPassword("zzangmin-db"));
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

        Column column = new Column("new_column", "VARCHAR(255)", false, null, false, false, "new column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType.ADD_COLUMN);

        //when & then
        Assertions.assertThrows(IllegalStateException.class, () -> ddlValidator.validateDDLRequest(backOfficeDatabaseConnectionInfo, addColumnRequestDTO));
    }

    @DisplayName("add column validation이 정상적으로 수행되어야 한다")
    @Test
    void validateAddColumnTest() {
        //given
        Column column = new Column("new_column", "VARCHAR(255)", false, null, false, false, "new column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType.ADD_COLUMN);
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateAddColumn(backOfficeDatabaseConnectionInfo, addColumnRequestDTO));
    }

    @DisplayName("add column 시 auto_increment 컬럼은 추가할 수 없다.")
    @Test
    void validateAddColumnTest_autoIncrement() {
        //given
        Column column = new Column("new_column", "VARCHAR(255)", false, null, false, true, "new column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType.ADD_COLUMN);
        //when & then

        Assertions.assertThrows(IllegalStateException.class, () -> ddlValidator.validateAddColumn(backOfficeDatabaseConnectionInfo, addColumnRequestDTO));
    }

    @DisplayName("alter column validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateAlterColumnTest() {
        //given
        Column column = new Column("name", "VARCHAR(255)", false, null, false, false, "alter column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AlterColumnRequestDTO alterColumnRequestDTO = new AlterColumnRequestDTO(schemaName, "test_table", "name", column);
        alterColumnRequestDTO.setCommandType(CommandType.ALTER_COLUMN);
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
        Column column1 = new Column("id", "INT", false, "0", false, true, "column1 comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name", "INT", false, "0", false, false, "column2 comment", "utf8mb4", "utf8mb4_0900_ai_ci");

        Constraint constraint1 = new Constraint("PRIMARY KEY", "id", List.of("id"));
        Constraint constraint2 = new Constraint("UNIQUE KEY", "name", List.of("name"));
        CreateTableRequestDTO createTableRequestDTO = new CreateTableRequestDTO(schemaName, "create_table_test", List.of(column1), List.of(constraint1, constraint2), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        createTableRequestDTO.setCommandType(CommandType.CREATE_TABLE);
        //when & then
        Assertions.assertDoesNotThrow(() -> ddlValidator.validateCreateTable(backOfficeDatabaseConnectionInfo, createTableRequestDTO));
    }



}