package zzangmin.db_automation.schedule.tableschemacheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Table;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


class TableDifferenceCheckerTest {

    @Mock
    private MysqlClient mysqlClient;

    private TableDifferenceChecker tableDifferenceChecker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tableDifferenceChecker = new TableDifferenceChecker(mysqlClient);
    }

    @Test
    void compareTableSchema() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "com.mysql.cj.jdbc.Driver", "testendpoint1", "admin", "123*");
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("inhouse", "com.mysql.cj.jdbc.Driver", "testendpoint1", "admin", "123*");

        when(mysqlClient.findSchemaNames(eq(prod))).thenReturn(List.of("test"));
        when(mysqlClient.findTableNames(any(), eq("test"))).thenReturn(List.of("test_table"));
        when(mysqlClient.findTables(eq(prod), eq("test"), any())).thenReturn(List.of(
                Table.builder()
                        .tableName("test_table")
                        .tableEngine("innoDB")
                        .columns(List.of(new Column()))
                        .build()));
        when(mysqlClient.findTables(eq(stage), eq("test"), any())).thenReturn(List.of(
                Table.builder()
                        .tableName("test_table")
                        .tableEngine("sdf")
                        .build()));
        // when
        String s = tableDifferenceChecker.compareTableSchema(prod, stage);

        // then
        System.out.println("s = " + s);

    }
}