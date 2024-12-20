package zzangmin.db_automation.schedule.mysqlobjectcheck;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.mysqlobject.Column;
import zzangmin.db_automation.entity.mysqlobject.Table;
import zzangmin.db_automation.service.SchemaObjectService;
import zzangmin.db_automation.testfactory.EntityFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


class TableDifferenceCheckerTest {

    @Mock
    private MysqlClient mysqlClient;
    @Mock
    private SchemaObjectService schemaObjectService;

    private TableDifferenceChecker tableDifferenceChecker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tableDifferenceChecker = new TableDifferenceChecker(mysqlClient, schemaObjectService);
    }

    @DisplayName("테이블 조회 결과가 같으면 문자열을 반환하지 않아야 한다.")
    @Test
    void compareTableSchema() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("stage", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);

        when(mysqlClient.findSchemaNames(eq(prod))).thenReturn(List.of("test"));
        when(mysqlClient.findTableNames(any(), eq("test"))).thenReturn(List.of("test_table"));
        when(mysqlClient.findTables(eq(prod), eq("test"), any())).thenReturn(List.of(EntityFactory.generateBasicTable("test_table")));
        when(mysqlClient.findTables(eq(stage), eq("test"), any())).thenReturn(List.of(EntityFactory.generateBasicTable("test_table")));

        // when
        String s = tableDifferenceChecker.compareTableSchema(prod, stage, List.of("test"));

        // then
        Assertions.assertThat(s).isBlank();

    }

    @DisplayName("테이블 조회 결과가 없으면 '찾을 수 없습니다.'를 포함한 문자열이 반환되어야 한다.")
    @Test
    void compareTableSchema_notable() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("stage", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);

        when(mysqlClient.findSchemaNames(eq(prod))).thenReturn(List.of("test"));
        when(mysqlClient.findTableNames(any(), eq("test"))).thenReturn(List.of("test_table"));
        when(mysqlClient.findTables(eq(prod), eq("test"), any())).thenReturn(List.of(EntityFactory.generateBasicTable("test_table")));
        when(mysqlClient.findTables(eq(stage), eq("test"), any())).thenReturn(List.of(EntityFactory.generateBasicTable("test_tablee")));

        // when
        String s = tableDifferenceChecker.compareTableSchema(prod, stage, List.of("test"));

        // then
        Assertions.assertThat(s).contains("찾을 수 없습니다.");

    }

    @DisplayName("컬럼 개수가 다르면 '컬럼 개수가 다릅니다'를 포함한 문자열이 반환되어야 한다.")
    @Test
    void compareTableSchema_different_differentcolumnsize() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("stage", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);

        Table prodTable = EntityFactory.generateBasicTable("basic_table");
        Table stageTable = EntityFactory.generateBasicTable("basic_table");
        stageTable.addColumns(List.of(EntityFactory.generateBasicColumn("extra_column")));

        when(mysqlClient.findSchemaNames(eq(prod))).thenReturn(List.of("test"));
        when(mysqlClient.findTableNames(any(), eq("test"))).thenReturn(List.of("basic_table"));
        when(mysqlClient.findTables(eq(prod), eq("test"), any())).thenReturn(List.of(prodTable));
        when(mysqlClient.findTables(eq(stage), eq("test"), any())).thenReturn(List.of(stageTable));

        // when
        String s = tableDifferenceChecker.compareTableSchema(prod, stage, List.of("test"));

        // then
        Assertions.assertThat(s).contains("컬럼 개수가 다릅니다");

    }

    @DisplayName("컬럼 개수가 다르면 '컬럼 개수가 다릅니다'를 포함한 문자열이 반환되어야 한다.")
    @Test
    void compareTableSchema_different_differentcolumn() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("stage", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);

        Table prodTable = EntityFactory.generateBasicTable("basic_table");
        Table stageTable = EntityFactory.generateBasicTable("basic_table");
        prodTable.addColumns(List.of(new Column("column_name", "varchar(123)", true, null, false, "comment", "utf8mb4_0900_ai_ci")));
        stageTable.addColumns(List.of(new Column("column_name", "varchar(124)", true, null, false, "asdfcomment", "utf8mb4_0900_ai_ci")));

        when(mysqlClient.findSchemaNames(eq(prod))).thenReturn(List.of("test"));
        when(mysqlClient.findTableNames(any(), eq("test"))).thenReturn(List.of("basic_table"));
        when(mysqlClient.findTables(eq(prod), eq("test"), any())).thenReturn(List.of(prodTable));
        when(mysqlClient.findTables(eq(stage), eq("test"), any())).thenReturn(List.of(stageTable));

        // when
        String s = tableDifferenceChecker.compareTableSchema(prod, stage, List.of("test"));

        // then
        Assertions.assertThat(s).contains("타입이 다릅니다");
        Assertions.assertThat(s).contains("코멘트가 다릅니다");

    }
}