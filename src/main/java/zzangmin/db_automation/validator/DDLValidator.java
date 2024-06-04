package zzangmin.db_automation.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.convention.ColumnConvention;
import zzangmin.db_automation.convention.CommonConvention;
import zzangmin.db_automation.convention.IndexConvention;
import zzangmin.db_automation.convention.TableConvention;
import zzangmin.db_automation.dto.request.ddl.*;
import zzangmin.db_automation.entity.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DDLValidator {

    private static final int LONG_QUERY_SECONDS_THRESHOLD = 3;
    private final MysqlClient mysqlClient;
    private final RdsMetricValidator rdsMetricValidator;
    private final TableStatusValidator tableStatusValidator;


    public void validateDDLRequest(DatabaseConnectionInfo databaseConnectionInfo, DDLRequestDTO ddlRequestDTO) {
        DatabaseRequestCommandGroup.CommandType commandType = ddlRequestDTO.extractCommandType();
        if (commandType.equals(DatabaseRequestCommandGroup.CommandType.ADD_COLUMN)) {
            validateAddColumn(databaseConnectionInfo, (AddColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.ALTER_COLUMN)) {
            validateAlterColumn(databaseConnectionInfo, (AlterColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_INDEX)) {
            validateCreateIndex(databaseConnectionInfo, (CreateIndexRequestDTO) ddlRequestDTO);
            return;
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_TABLE)) {
            validateCreateTable(databaseConnectionInfo, (CreateTableRequestDTO) ddlRequestDTO);
            return;
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.DELETE_COLUMN)) {
            validateDeleteColumn(databaseConnectionInfo, (DeleteColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.EXTEND_VARCHAR_COLUMN)) {
            validateExtendVarchar(databaseConnectionInfo, (ExtendVarcharColumnRequestDTO) ddlRequestDTO);
            return;
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.RENAME_COLUMN)) {
            validateRenameColumn(databaseConnectionInfo, (RenameColumnRequestDTO) ddlRequestDTO);
            return ;
        }
        throw new IllegalArgumentException("CommandType 미지원: " + commandType);
    }

    public void validateAlterColumn(DatabaseConnectionInfo databaseConnectionInfo, AlterColumnRequestDTO alterColumnRequestDTO) {
        ColumnConvention.validateColumnConvention(alterColumnRequestDTO.getAfterColumn());
        validateIsSchemaExists(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateAddColumn(DatabaseConnectionInfo databaseConnectionInfo, AddColumnRequestDTO addColumnRequestDTO) {
        validateAddColumnHasAutoIncrementOption(addColumnRequestDTO.getColumn());
        ColumnConvention.validateColumnConvention(addColumnRequestDTO.getColumn());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());        tableStatusValidator.validateTableSize(databaseConnectionInfo, addColumnRequestDTO.getSchemaName(), addColumnRequestDTO.getTableName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateCreateIndex(DatabaseConnectionInfo databaseConnectionInfo, CreateIndexRequestDTO createIndexRequestDTO) {
        IndexConvention.validateIndexConvention(createIndexRequestDTO.toConstraint());
        validateIsSchemaExists(databaseConnectionInfo, createIndexRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        validateIsIndexExists(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName(), createIndexRequestDTO.getColumnNames());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
        validateCreateIndexType(createIndexRequestDTO.getIndexType());
    }

    public void validateExtendVarchar(DatabaseConnectionInfo databaseConnectionInfo, ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO) {
        Column column = mysqlClient.findColumn(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName(), extendVarcharColumnRequestDTO.getOldColumn().getName())
                .orElseThrow(() -> new IllegalArgumentException("컬럼 정보를 불러올 수 없습니다. 존재하지 않는 컬럼명: "+ extendVarcharColumnRequestDTO.getOldColumn().getName()));
        ColumnConvention.validateExtendVarcharConvention(column, extendVarcharColumnRequestDTO.getExtendSize());
        validateIsSchemaExists(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateCreateTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        Table table = new Table(createTableRequestDTO.getTableName(), createTableRequestDTO.getColumns(), createTableRequestDTO.getConstraints(), createTableRequestDTO.getEngine(), createTableRequestDTO.getCharset(), createTableRequestDTO.getCollate(), createTableRequestDTO.getTableComment());
        TableConvention.validateTableConvention(table);
        validateIsSchemaExists(databaseConnectionInfo, createTableRequestDTO.getSchemaName());
        validateIsNotExistTableName(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
    }

    public void validateDeleteColumn(DatabaseConnectionInfo databaseConnectionInfo, DeleteColumnRequestDTO deleteColumnRequestDTO) {
        validateIsSchemaExists(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName());
        validateIsExistColumnName(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName(), deleteColumnRequestDTO.getColumnName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateRenameColumn(DatabaseConnectionInfo databaseConnectionInfo, RenameColumnRequestDTO ddlRequestDTO) {
        ColumnConvention.validateColumnNamingConvention(ddlRequestDTO.getAfterColumnName());
        validateIsSchemaExists(databaseConnectionInfo, ddlRequestDTO.getSchemaName());
        validateIsExistColumnName(databaseConnectionInfo, ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), ddlRequestDTO.getBeforeColumnName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    public void validateRenameTable(DatabaseConnectionInfo databaseConnectionInfo, RenameTableRequestDTO ddlRequestDTO) {
        CommonConvention.validateReservedWord(ddlRequestDTO.getNewTableName());
        CommonConvention.validateSnakeCase(ddlRequestDTO.getNewTableName());
        CommonConvention.validateLowerCaseString(ddlRequestDTO.getNewTableName());
        validateIsSchemaExists(databaseConnectionInfo, ddlRequestDTO.getSchemaName());
        validateIsExistTableName(databaseConnectionInfo, ddlRequestDTO.getSchemaName(), ddlRequestDTO.getOldTableName());
        validateIsNotExistTableName(databaseConnectionInfo, ddlRequestDTO.getSchemaName(), ddlRequestDTO.getNewTableName());
        tableStatusValidator.validateTableSize(databaseConnectionInfo, ddlRequestDTO.getSchemaName(), ddlRequestDTO.getOldTableName());
        rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
        validateIsLongQueryExists(databaseConnectionInfo);
    }

    private void validateIsNotExistTableName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        if (schemaName == null || schemaName == "" || schemaName == " ") {
            throw new IllegalArgumentException("schemaName 이 비어있습니다.");
        }
        if (tableName == null || tableName == "" || tableName == " ") {
            throw new IllegalArgumentException("tableName 이 비어있습니다.");
        }
        List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
        if (tableNames.contains(tableName)) {
            throw new IllegalStateException("이미 존재하는 테이블입니다.");
        }
    }

    private void validateIsExistTableName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        if (schemaName == null || schemaName == "" || schemaName == " ") {
            throw new IllegalArgumentException("schemaName 이 비어있습니다.");
        }
        if (tableName == null || tableName == "" || tableName == " ") {
            throw new IllegalArgumentException("tableName 이 비어있습니다.");
        }
        List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
        if (!tableNames.contains(tableName)) {
            throw new IllegalStateException("대상 테이블이 존재하지 않습니다.");
        }
    }

    private void validateIsExistColumnName(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName, String columnName) {
        if (schemaName == null || schemaName == "" || schemaName == " ") {
            throw new IllegalArgumentException("schemaName 이 비어있습니다.");
        }
        if (tableName == null || tableName == "" || tableName == " ") {
            throw new IllegalArgumentException("tableName 이 비어있습니다.");
        }
        if (columnName == null || columnName == "" || columnName == " ") {
            throw new IllegalArgumentException("columnName 이 비어있습니다.");
        }
        mysqlClient.findColumn(databaseConnectionInfo, schemaName, tableName, columnName)
                .orElseThrow(() -> new IllegalArgumentException("컬럼 정보를 불러올 수 없습니다. 존재하지 않는 컬럼명: " + columnName));
    }


    private void validateIsSchemaExists(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        if (schemaName == null || schemaName == "" || schemaName == " ") {
            throw new IllegalArgumentException("schemaName 이 비어있습니다.");
        }
        List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo);
        if (!schemaNames.contains(schemaName)) {
            throw new IllegalStateException("존재하지 않는 스키마입니다.");
        }
    }

    private void validateIsLongQueryExists(DatabaseConnectionInfo databaseConnectionInfo) {
        List<MysqlProcess> longQueries = mysqlClient.findLongQueries(databaseConnectionInfo, LONG_QUERY_SECONDS_THRESHOLD);
        log.info("longQueries = " + longQueries);
        if (longQueries.size() != 0) {
            throw new IllegalStateException("실행중인 long query 가 존재합니다.");
        }
    }

    private void validateIsIndexExists(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName, List<String> columnNames) {
        if (schemaName == null || schemaName == "" || schemaName == " ") {
            throw new IllegalArgumentException("schemaName 이 비어있습니다.");
        }
        if (tableName == null || tableName == "" || tableName == " ") {
            throw new IllegalArgumentException("tableName 이 비어있습니다.");
        }
        List<Constraint> indexes = mysqlClient.findIndexes(databaseConnectionInfo, schemaName, tableName);
        for (Constraint index : indexes) {
            if (isEqualListString(index.getKeyColumnNames(), columnNames)) {
                throw new IllegalStateException("이미 존재하는 컬럼 조합의 인덱스가 존재합니다. 인덱스명: " + index.getKeyName());
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

    private void validateAddColumnHasAutoIncrementOption(Column column) {
        if (column == null) {
            throw new IllegalArgumentException("column 이 null 입니다.");
        }
        if (column.getIsAutoIncrement() == true) {
            throw new IllegalStateException("auto_increment 옵션이 있는 컬럼은 추가할 수 없습니다.");
        }
    }

    private void validateCreateIndexType(String indexType) {
        if (indexType == null || indexType == "" || indexType == " ") {
            throw new IllegalArgumentException("인덱스 type 이 null 입니다.");
        }
        if (indexType.equals("KEY") || indexType.equals("UNIQUE")) {
            return;
        }
        throw new IllegalArgumentException("지원하지 않는 인덱스 타입입니다.");
    }
}
