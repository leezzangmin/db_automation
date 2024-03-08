package zzangmin.db_automation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.AddColumnRequestDTO;
import zzangmin.db_automation.dto.request.CreateChangeHistoryRequestDTO;
import zzangmin.db_automation.entity.ChangeHistory;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChangeHistoryServiceTest {

    @Autowired
    private ChangeHistoryService changeHistoryService;

    @Autowired
    private MysqlClient mysqlClient;

    @Autowired
    private AwsService awsService;

    private DatabaseConnectionInfo backOfficeDatabaseConnectionInfo;
    private String schemaName = "test_schema";

    @BeforeEach
    public void setUp() {
//        backOfficeDatabaseConnectionInfo = new DatabaseConnectionInfo("zzangmin-db", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://zzangmin-db.codf49uhek24.ap-northeast-2.rds.amazonaws.com", "admin", awsService.findRdsPassword("zzangmin-db"));
        backOfficeDatabaseConnectionInfo = new DatabaseConnectionInfo();
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DELETE FROM automation_change_history.change_history where table_name = 'test_table'");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE TABLE test_schema.test_table (id INT NOT NULL AUTO_INCREMENT COMMENT 'asdf', name VARCHAR(45) NULL COMMENT 'name comment', PRIMARY KEY (id), KEY name(name)) COMMENT 'TABLE COMMENT'");
    }

    @AfterEach
    public void tearDown() {
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
    }

    @DisplayName("변경 이력 저장/조회 성공 테스트")
    @Test
    void addChangeHistoryTest() {
        //given
        CreateChangeHistoryRequestDTO createChangeHistoryRequestDTO = new CreateChangeHistoryRequestDTO(CommandType.ADD_COLUMN, backOfficeDatabaseConnectionInfo.getDatabaseName(), schemaName, "test_table", "test_doer@gmail.com", LocalDateTime.now());
        Column column = new Column("add", "INT", false, null, false, false, "add column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType.ADD_COLUMN);
        //when
        changeHistoryService.addChangeHistory(createChangeHistoryRequestDTO, addColumnRequestDTO);
        //then
        List<ChangeHistory> changeHistories = changeHistoryService.findChangeHistories(backOfficeDatabaseConnectionInfo.getDatabaseName(), schemaName, "test_table");
        assertEquals(1, changeHistories.size());

        ChangeHistory changeHistory = changeHistories.get(0);
        assertEquals(CommandType.ADD_COLUMN, changeHistory.getCommandType());
        assertEquals(backOfficeDatabaseConnectionInfo.getDatabaseName(), changeHistory.getDatabaseIdentifier());
        assertEquals(schemaName, changeHistory.getSchemaName());
        assertEquals("test_table", changeHistory.getTableName());

    }
}