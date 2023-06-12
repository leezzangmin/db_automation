package zzangmin.db_automation.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.convention.IndexConvention;
import zzangmin.db_automation.convention.TableConvention;
import zzangmin.db_automation.dto.request.CreateIndexRequestDTO;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.MysqlProcess;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class DDLValidator {

    private static final int LONG_QUERY_SECONDS_THRESHOLD = 10;
    private final MysqlClient mysqlClient;
    private final TableConvention tableConvention;
    private final IndexConvention indexConvention;
    private final RdsMetricValidator rdsMetricValidator;
    private final TableStatusValidator tableStatusValidator;

    public void validateCreateIndex(DatabaseConnectionInfo databaseConnectionInfo, CreateIndexRequestDTO createIndexRequestDTO) {
        indexConvention.validateIndexConvention(createIndexRequestDTO.getIndexName(), createIndexRequestDTO.getColumnNames());
        validateIsSchemaExists(databaseConnectionInfo, createIndexRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    /**
     * 0. 요청 schema 존재여부
     * 1. 테이블 이미 존재여부
     * 2. cpu, memory 사용량
     * 3. 롱쿼리(트랜잭션)
     * 4. 테이블 status 임계치
     */
    public void validateCreateTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        tableConvention.validateTableConvention(createTableRequestDTO.getColumns(), createTableRequestDTO.getConstraints(), createTableRequestDTO.getTableName(), createTableRequestDTO.getEngine(), createTableRequestDTO.getCharset(), createTableRequestDTO.getCollate(), createTableRequestDTO.getTableComment());
        validateIsSchemaExists(databaseConnectionInfo, createTableRequestDTO.getSchemaName());
        validateIsNotExistTableName(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        // validateIsLongQueryExists(databaseConnectionInfo);
    }

    private void validateIsNotExistTableName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        Set<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
        if (tableNames.contains(tableName)) {
            throw new IllegalStateException("이미 존재하는 테이블입니다.");
        }
    }

    private void validateIsExistTableName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        Set<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
        if (!tableNames.contains(tableName)) {
            throw new IllegalStateException("대상 테이블이 존재하지 않습니다.");
        }
    }


    private void validateIsSchemaExists(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        Set<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo);
        if (!schemaNames.contains(schemaName)) {
            throw new IllegalStateException("존재하지 않는 스키마입니다.");
        }
    }

    private void validateIsLongQueryExists(DatabaseConnectionInfo databaseConnectionInfo) {
        List<MysqlProcess> longQueries = mysqlClient.findLongQueries(databaseConnectionInfo, LONG_QUERY_SECONDS_THRESHOLD);
        if (longQueries.size() != 0) {
            throw new IllegalStateException("실행중인 long query 가 존재합니다.");
        }
    }

    private void validateIsIndexExists(DatabaseConnectionInfo databaseConnectionInfo, String indexName, List<Column> columns) {

    }
}
