package zzangmin.db_automation.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.convention.TableConvention;
import zzangmin.db_automation.dto.CreateTableRequestDTO;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class DDLValidator {

    private static final int LONG_QUERY_SECONDS_THRESHOLD = 10;
    private final MysqlClient mysqlClient;
    private final TableConvention tableConvention;
    private final RdsMetricValidator rdsMetricValidator;

    /**
     * 1. 테이블 이미 존재여부
     * 2. cpu, memory 사용량
     * 3. 롱쿼리(트랜잭션)
     * 4. 테이블 status 임계치
     */

    public void validateCreateTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        tableConvention.validateTableConvention(createTableRequestDTO);
        validateIsExistTableName(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }
    private void validateIsExistTableName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        Set<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
        if (tableNames.contains(tableName)) {
            throw new IllegalStateException("이미 존재하는 테이블입니다.");
        }
    }

    private void validateIsLongQueryExists(DatabaseConnectionInfo databaseConnectionInfo) {
        mysqlClient.findLongQueries(databaseConnectionInfo, LONG_QUERY_SECONDS_THRESHOLD);
    }




}
