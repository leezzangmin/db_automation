package zzangmin.db_automation.validator;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.AddColumnRequestDTO;
import zzangmin.db_automation.dto.request.AlterColumnRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DELETE FROM automation_change_history.change_history where table_name = 'test_table'");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE TABLE test_schema.test_table (id INT NOT NULL AUTO_INCREMENT COMMENT 'asdf', name VARCHAR(45) NULL COMMENT 'name comment', PRIMARY KEY (id), KEY name(name)) COMMENT 'TABLE COMMENT'");
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



}