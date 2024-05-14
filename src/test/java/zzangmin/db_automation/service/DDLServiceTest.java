package zzangmin.db_automation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.testfactory.DatabaseConnectionInfoFactory;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.entity.Table;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
class DDLServiceTest {

    @Autowired
    private DatabaseConnectionInfoFactory databaseConnectionInfoFactory;

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
        backOfficeDatabaseConnectionInfo = databaseConnectionInfoFactory.createDatabaseConnectionInfo();
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
        extendVarcharColumnRequestDTO.setCommandType(CommandType_old.EXTEND_VARCHAR_COLUMN);
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
        Column column1 = new Column("id", "INT", false, "0", true, "column1 comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.UNIQUE, "id", List.of("id"));
        CreateTableRequestDTO createTableRequestDTO = new CreateTableRequestDTO(schemaName, "create_table_test", Set.of(column1), Set.of(constraint1, constraint2), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        createTableRequestDTO.setCommandType(CommandType_old.CREATE_TABLE);
        //when
        ddlService.createTable(backOfficeDatabaseConnectionInfo, createTableRequestDTO);
        //then
        Table findTable = mysqlClient.findTables(backOfficeDatabaseConnectionInfo, schemaName, List.of("create_table_test")).get(0);
        Column findColumn = findTable.getColumns().stream().collect(Collectors.toList()).get(0);
        Constraint findConstraint0 = findTable.getConstraints().stream().collect(Collectors.toList()).get(0);
        Constraint findConstraint1 = findTable.getConstraints().stream().collect(Collectors.toList()).get(1);

        Assertions.assertThat(findTable.getTableName()).isEqualTo("create_table_test");
        Assertions.assertThat(findTable.getTableComment()).isEqualTo("table comment");
        Assertions.assertThat(findTable.getTableEngine()).isEqualTo("InnoDB");

        Assertions.assertThat(findColumn.getName()).isEqualTo("id");
        Assertions.assertThat(findColumn.getType()).startsWithIgnoringCase("int");
        Assertions.assertThat(findColumn.getIsNull()).isEqualTo(false);
        Assertions.assertThat(findColumn.getDefaultValue()).isEqualTo(null);
        Assertions.assertThat(findColumn.getIsAutoIncrement()).isEqualTo(true);
        Assertions.assertThat(findColumn.getComment()).isEqualTo("column1 comment");
        Assertions.assertThat(findColumn.getCharset()).isEqualTo("utf8mb4");
        Assertions.assertThat(findColumn.getCollate()).isEqualTo("utf8mb4_0900_ai_ci");

        Assertions.assertThat(findConstraint0.getKeyName()).startsWith("PRIMARY");
        Assertions.assertThat(findConstraint1.getKeyName()).isEqualTo("id");
        Assertions.assertThat(findConstraint0.getKeyColumnNames()).isEqualTo(List.of("id"));



    }

    @DisplayName("create index 성공 테스트")
    @Test
    void createIndex() {
        //given
        CreateIndexRequestDTO createIndexRequestDTO = new CreateIndexRequestDTO(schemaName, "test_table", "id_name", "KEY", List.of("id", "name"));
        createIndexRequestDTO.setCommandType(CommandType_old.CREATE_INDEX);
        //when
        ddlService.createIndex(backOfficeDatabaseConnectionInfo, createIndexRequestDTO);
        //then
        List<Constraint> indexes = mysqlClient.findIndexes(backOfficeDatabaseConnectionInfo, schemaName, "test_table");
        Assertions.assertThat(indexes.size()).isEqualTo(3);
    }

    @DisplayName("add column 성공 테스트")
    @Test
    void addColumn() {
        //given
        Column column = new Column("add", "INT", false, null, false, "add column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, "test_table", column);
        addColumnRequestDTO.setCommandType(CommandType_old.ADD_COLUMN);
        //when
        ddlService.addColumn(backOfficeDatabaseConnectionInfo, addColumnRequestDTO);
        //then
        Column findColumn = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, "test_table", "add").get();
        Assertions.assertThat(findColumn.getName()).isEqualTo("add");
        Assertions.assertThat(findColumn.getType()).startsWithIgnoringCase("int");
        Assertions.assertThat(findColumn.getIsNull()).isEqualTo(false);
        Assertions.assertThat(findColumn.getDefaultValue()).isEqualTo(null);
        Assertions.assertThat(findColumn.getIsAutoIncrement()).isEqualTo(false);
        Assertions.assertThat(findColumn.getComment()).isEqualTo("add column comment");
        Assertions.assertThat(findColumn.getCharset()).isEqualTo("utf8mb4");
        Assertions.assertThat(findColumn.getCollate()).isEqualTo("utf8mb4_0900_ai_ci");
    }

    @DisplayName("delete column 성공 테스트")
    @Test
    void deleteColumn() {
        //given
        DeleteColumnRequestDTO deleteColumnRequestDTO = new DeleteColumnRequestDTO(schemaName, "test_table", "name");
        deleteColumnRequestDTO.setCommandType(CommandType_old.DELETE_COLUMN);
        //when
        ddlService.deleteColumn(backOfficeDatabaseConnectionInfo, deleteColumnRequestDTO);
        //then
        Optional<Column> findColumn = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, "test_table", "name");
        Assertions.assertThat(findColumn.isPresent()).isEqualTo(false);
    }

    @DisplayName("alter column 성공 테스트")
    @Test
    void alterColumn() {
        //given
        Column column = new Column("name", "VARCHAR(255)", false, null, false, "alter column comment", "utf8mb4", "utf8mb4_0900_ai_ci");
        AlterColumnRequestDTO alterColumnRequestDTO = new AlterColumnRequestDTO(schemaName, "test_table", "name", column);
        alterColumnRequestDTO.setCommandType(CommandType_old.ALTER_COLUMN);
        //when
        ddlService.alterColumn(backOfficeDatabaseConnectionInfo, alterColumnRequestDTO);
        //then
        Column findColumn = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, "test_table", "name").get();
        Assertions.assertThat(findColumn.getName()).isEqualTo("name");
        Assertions.assertThat(findColumn.getType()).isEqualTo("varchar(255)");
        Assertions.assertThat(findColumn.getIsNull()).isEqualTo(false);
        Assertions.assertThat(findColumn.getDefaultValue()).isEqualTo(null);
        Assertions.assertThat(findColumn.getIsAutoIncrement()).isEqualTo(false);
        Assertions.assertThat(findColumn.getComment()).isEqualTo("alter column comment");
        Assertions.assertThat(findColumn.getCharset()).isEqualTo("utf8mb4");
        Assertions.assertThat(findColumn.getCollate()).isEqualTo("utf8mb4_0900_ai_ci");
    }

    @DisplayName("rename column 성공 테스트")
    @Test
    void renameColumn() {
        //given
        RenameColumnRequestDTO renameColumnRequestDTO = new RenameColumnRequestDTO(schemaName, "test_table", "name", "rename");
        renameColumnRequestDTO.setCommandType(CommandType_old.RENAME_COLUMN);
        //when
        ddlService.renameColumn(backOfficeDatabaseConnectionInfo, renameColumnRequestDTO);
        //then
        Optional<Column> beforeColumn = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, "test_table", "name");
        Assertions.assertThat(beforeColumn.isPresent()).isEqualTo(false);
        Optional<Column> findColumn = mysqlClient.findColumn(backOfficeDatabaseConnectionInfo, schemaName, "test_table", "rename");
        Assertions.assertThat(findColumn.isPresent()).isEqualTo(true);
    }
}