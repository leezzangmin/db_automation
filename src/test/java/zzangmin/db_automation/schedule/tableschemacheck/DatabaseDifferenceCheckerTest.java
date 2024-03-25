package zzangmin.db_automation.schedule.tableschemacheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.SchemaObjectService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DatabaseDifferenceCheckerTest {

    @Mock
    private MysqlClient mysqlClient;
    @Mock
    private SchemaObjectService schemaObjectService;
    private DatabaseDifferenceChecker databaseDifferenceChecker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseDifferenceChecker = new DatabaseDifferenceChecker(mysqlClient, schemaObjectService);
    }

    @DisplayName("compareDatabase로 database 생성 구문이 같으면 빈 String이 반환된다")
    @Test
    void testCompareDatabase() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "com.mysql.cj.jdbc.Driver", "testendpoint1", "admin", "123*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("inhouse", "com.mysql.cj.jdbc.Driver", "testendpoint1", "admin", "123*", null);
        when(mysqlClient.findSchemaNames(any())).thenReturn(List.of("test_schema123"));
        when(mysqlClient.findCreateDatabaseStatement(any(),any())).thenReturn(Optional.of("create database"));

        // when
        String differenceResult = databaseDifferenceChecker.compareDatabase(prod, stage);

        //then
        assertThat(differenceResult).isBlank();

    }

    @DisplayName("compareDatabase로 database 생성 구문이 다르면 비어있지 않은 String이 반환된다")
    @Test
    void testDifferentCompareDatabase() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "com.mysql.cj.jdbc.Driver", "testendpoint1", "admin", "123*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("inhouse", "com.mysql.cj.jdbc.Driver", "testendpoint1", "admin", "123*", null);

        when(mysqlClient.findSchemaNames(any())).thenReturn(List.of("test_schema123"));
        when(mysqlClient.findCreateDatabaseStatement(eq(prod),any())).thenReturn(Optional.of("create database fff;"));
        when(mysqlClient.findCreateDatabaseStatement(eq(stage),any())).thenReturn(Optional.of("create database aaa;"));

        // when
        String differenceResult = databaseDifferenceChecker.compareDatabase(prod, stage);

        //then
        assertThat(differenceResult).isNotBlank();
    }
}