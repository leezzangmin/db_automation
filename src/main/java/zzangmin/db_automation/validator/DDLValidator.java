package zzangmin.db_automation.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.convention.ColumnConvention;
import zzangmin.db_automation.convention.IndexConvention;
import zzangmin.db_automation.convention.TableConvention;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.entity.MysqlProcess;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.List;
import java.util.Map;
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
    private final ColumnConvention columnConvention;

    public void validateDDLRequest(DatabaseConnectionInfo databaseConnectionInfo, DDLRequestDTO ddlRequestDTO) {
        if (ddlRequestDTO.getCommandType().equals(CommandType.ADD_COLUMN)) {
            validateAddColumn(databaseConnectionInfo, (AddColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.ALTER_COLUMN)) {
            validateAlterColumn(databaseConnectionInfo, (AlterColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.CREATE_INDEX)) {
            validateCreateIndex(databaseConnectionInfo, (CreateIndexRequestDTO) ddlRequestDTO);
            return;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.CREATE_TABLE)) {
            validateCreateTable(databaseConnectionInfo, (CreateTableRequestDTO) ddlRequestDTO);
            return;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.ADD_COLUMN)) {
            validateAddColumn(databaseConnectionInfo, (AddColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.DELETE_COLUMN)) {
            validateDeleteColumn(databaseConnectionInfo, (DeleteColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.EXTEND_VARCHAR_COLUMN)) {
            validateExtendVarchar(databaseConnectionInfo, (ExtendVarcharColumnRequestDTO) ddlRequestDTO);
            return;
        }
        throw new IllegalArgumentException("CommandType 지원 불가");
    }

    public void validateAlterColumn(DatabaseConnectionInfo databaseConnectionInfo, AlterColumnRequestDTO alterColumnRequestDTO) {
        columnConvention.validateColumnConvention(alterColumnRequestDTO.getAfterColumn());
        validateIsSchemaExists(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateAddColumn(DatabaseConnectionInfo databaseConnectionInfo, AddColumnRequestDTO addColumnRequestDTO) {
        columnConvention.validateColumnConvention(addColumnRequestDTO.getColumn());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, addColumnRequestDTO.getSchemaName(), addColumnRequestDTO.getTableName());
        validateIsLongQueryExists(databaseConnectionInfo);

    }

    public void validateCreateIndex(DatabaseConnectionInfo databaseConnectionInfo, CreateIndexRequestDTO createIndexRequestDTO) {
        indexConvention.validateIndexConvention(createIndexRequestDTO.toConstraint());
        validateIsSchemaExists(databaseConnectionInfo, createIndexRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        validateIsIndexExists(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName(), createIndexRequestDTO.getColumnNames());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateExtendVarchar(DatabaseConnectionInfo databaseConnectionInfo, ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO) {
        Column column = mysqlClient.findColumn(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName(), extendVarcharColumnRequestDTO.getColumn().getName())
                .orElseThrow(() -> new IllegalArgumentException("컬럼 정보를 불러올 수 없습니다. 존재하지 않는 컬럼명: "+ extendVarcharColumnRequestDTO.getColumn().getName()));
        columnConvention.validateExtendVarcharConvention(column, extendVarcharColumnRequestDTO.getColumn().getVarcharLength());
        validateIsSchemaExists(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateCreateTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        tableConvention.validateTableConvention(createTableRequestDTO.getColumns(), createTableRequestDTO.getConstraints(), createTableRequestDTO.getTableName(), createTableRequestDTO.getEngine(), createTableRequestDTO.getCharset(), createTableRequestDTO.getCollate(), createTableRequestDTO.getTableComment());
        validateIsSchemaExists(databaseConnectionInfo, createTableRequestDTO.getSchemaName());
        validateIsNotExistTableName(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
    }

    public void validateDeleteColumn(DatabaseConnectionInfo databaseConnectionInfo, DeleteColumnRequestDTO deleteColumnRequestDTO) {
        validateIsSchemaExists(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName());
        validateIsExistColumnName(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName(), deleteColumnRequestDTO.getColumnName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
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

    private void validateIsExistColumnName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName, String columnName) {
        mysqlClient.findColumn(databaseConnectionInfo, schemaName, tableName, columnName)
                .orElseThrow(() -> new IllegalArgumentException("컬럼 정보를 불러올 수 없습니다. 존재하지 않는 컬럼명: " + columnName));
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

    private void validateIsIndexExists(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName, List<String> columnNames) {
        Map<String, List<String>> indexes = mysqlClient.findIndexes(databaseConnectionInfo, schemaName, tableName);
        for (String indexNameKey : indexes.keySet()) {
            List<String> indexColumnNames = indexes.get(indexNameKey);
            if (isEqualListString(indexColumnNames, columnNames)) {
                throw new IllegalStateException("이미 존재하는 컬럼 조합의 인덱스가 존재합니다. 인덱스명: " + indexNameKey);
            }
        }
    }

    private boolean isEqualListString(List<String> listA, List<String> listB) {
        if (listA.size() != listB.size()) {
            return false;
        }
        int size = listA.size();
        for (int i = 0; i < size; i++) {
            if (!listA.get(i).equals(listB.get(i))) {
                return false;
            }
        }
        return true;
    }

}
