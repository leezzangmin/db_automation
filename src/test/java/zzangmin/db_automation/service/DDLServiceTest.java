package zzangmin.db_automation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.dto.request.ExtendVarcharColumnRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.entity.Table;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DDLServiceTest {

    @Autowired
    private DDLService ddlService;

    @Autowired
    private MysqlClient mysqlClient;

    @Autowired
    private AwsService awsService;

    private DatabaseConnectionInfo backOfficeDatabaseConnectionInfo;
    private String schemaName = "test_schema";

    @BeforeEach
    public void setUp() {
        backOfficeDatabaseConnectionInfo = new DatabaseConnectionInfo("zzangmin-db", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://zzangmin-db.codf49uhek24.ap-northeast-2.rds.amazonaws.com", "admin", awsService.findRdsPassword("zzangmin-db"));
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "CREATE TABLE test_schema.test_table (id INT NOT NULL AUTO_INCREMENT COMMENT 'asdf', name VARCHAR(45) NULL COMMENT 'name comment', PRIMARY KEY (id), KEY name(name)) COMMENT 'TABLE COMMENT'");
    }

    @AfterEach
    public void tearDown() {
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.test_table");
        mysqlClient.executeSQL(backOfficeDatabaseConnectionInfo, "DROP TABLE IF EXISTS test_schema.create_table_test");
    }

    @DisplayName("varchar extend 성공 테스트")
    @Test
    void extendVarcharColumn() {
        //given
        ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO = new ExtendVarcharColumnRequestDTO("test_schema", "test_table", "name", 50);
        extendVarcharColumnRequestDTO.setCommandType(CommandType.EXTEND_VARCHAR_COLUMN);
        //when
        ddlService.extendVarcharColumn(backOfficeDatabaseConnectionInfo, extendVarcharColumnRequestDTO);
        //then
        Column findColumn = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, "test_table", "name").get();
        int varcharLength = findColumn.injectVarcharLength();
        Assertions.assertThat(varcharLength).isEqualTo(50);
    }

    @DisplayName("create table 성공 테스트")
    @Test
    void createTable() {
        //given
        Column column1 = new Column("id", "INT", false, "0", false, true, "column1 comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint("PRIMARY KEY", "id", List.of("id"));
        Constraint constraint2 = new Constraint("UNIQUE KEY", "id", List.of("id"));
        CreateTableRequestDTO createTableRequestDTO = new CreateTableRequestDTO(schemaName, "create_table_test", List.of(column1), List.of(constraint1, constraint2), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        createTableRequestDTO.setCommandType(CommandType.CREATE_TABLE);
        //when
        ddlService.createTable(backOfficeDatabaseConnectionInfo, createTableRequestDTO);
        //then
        Table findTable = mysqlClient.findTables(backOfficeDatabaseConnectionInfo, schemaName, List.of("create_table_test")).get(0);
        Assertions.assertThat(findTable.getTableName()).isEqualTo("create_table_test");
        Assertions.assertThat(findTable.getTableComment()).isEqualTo("table comment");
        Assertions.assertThat(findTable.getColumns().get(0).getName()).isEqualTo("id");
        Assertions.assertThat(findTable.getColumns().get(0).getType()).startsWithIgnoringCase("int");
        Assertions.assertThat(findTable.getColumns().get(0).isNull()).isEqualTo(false);
        Assertions.assertThat(findTable.getColumns().get(0).getDefaultValue()).isEqualTo(null);
        Assertions.assertThat(findTable.getColumns().get(0).isAutoIncrement()).isEqualTo(true);
        Assertions.assertThat(findTable.getColumns().get(0).getComment()).isEqualTo("column1 comment");
        Assertions.assertThat(findTable.getColumns().get(0).getCharset()).isEqualTo("utf8mb4");
        Assertions.assertThat(findTable.getColumns().get(0).getCollate()).isEqualTo("utf8mb4_0900_ai_ci");
        Assertions.assertThat(findTable.getConstraints().get(0).getKeyName()).startsWith("PRIMARY");
        Assertions.assertThat(findTable.getConstraints().get(1).getKeyName()).isEqualTo("id");
        Assertions.assertThat(findTable.getConstraints().get(0).getKeyColumnNames()).isEqualTo(List.of("id"));
        Assertions.assertThat(findTable.getTableEngine()).isEqualTo("InnoDB");


    }

}