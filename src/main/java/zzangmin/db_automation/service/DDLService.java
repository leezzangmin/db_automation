package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.dto.response.*;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.parser.DDLParser;

@Slf4j
@RequiredArgsConstructor
@Service
public class DDLService {

    private final DDLParser ddlParser;
    private final MysqlClient mysqlClient;

    public ExtendVarcharColumnDDLResponseDTO extendVarcharColumn(DatabaseConnectionInfo databaseConnectionInfo, ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO) {
        Column column = mysqlClient.findColumn(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName(), extendVarcharColumnRequestDTO.getTargetColumnName())
                .orElseThrow(() -> new IllegalArgumentException("컬럼 정보를 불러올 수 없습니다. 존재하지 않는 컬럼명: "+ extendVarcharColumnRequestDTO.getTargetColumnName()));
        column.changeColumnType("varchar(" + extendVarcharColumnRequestDTO.getExtendSize() + ")");
        String extendVarcharSQL = ddlParser.extendVarcharColumnCommandToSql(extendVarcharColumnRequestDTO, column);
        mysqlClient.executeSQL(databaseConnectionInfo, extendVarcharSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName());
        return new ExtendVarcharColumnDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName(), createTableStatement);
    }

    public CreateTableDDLResponseDTO createTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        String createTableSQL = ddlParser.commandToSql(createTableRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, createTableSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        return new CreateTableDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName(), createTableStatement);
    }

    public CreateIndexDDLResponseDTO createIndex(DatabaseConnectionInfo databaseConnectionInfo, CreateIndexRequestDTO createIndexRequestDTO) {
        String createIndexSQL = ddlParser.commandToSql(createIndexRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, createIndexSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        return new CreateIndexDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName(), createIndexRequestDTO.getIndexName(), createTableStatement);
    }

    public AddColumnDDLResponseDTO addColumn(DatabaseConnectionInfo databaseConnectionInfo, AddColumnRequestDTO addColumnRequestDTO) {
        String addColumnSQL = ddlParser.commandToSql(addColumnRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, addColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, addColumnRequestDTO.getSchemaName(), addColumnRequestDTO.getTableName());
        return new AddColumnDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), addColumnRequestDTO.getSchemaName(), addColumnRequestDTO.getTableName(), addColumnRequestDTO.getColumn().getName(), createTableStatement);
    }

    public DeleteColumnDDLResponseDTO deleteColumn(DatabaseConnectionInfo databaseConnectionInfo, DeleteColumnRequestDTO deleteColumnRequestDTO) {
        String deleteColumnSQL = ddlParser.commandToSql(deleteColumnRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, deleteColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName());
        return new DeleteColumnDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName(), deleteColumnRequestDTO.getColumnName(), createTableStatement);
    }

    public AlterColumnDDLResponseDTO alterColumn(DatabaseConnectionInfo databaseConnectionInfo, AlterColumnRequestDTO alterColumnRequestDTO) {
        String alterColumnSQL = ddlParser.commandToSql(alterColumnRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, alterColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName());
        return new AlterColumnDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName(), createTableStatement);
    }

    public RenameColumnDDLResponseDTO renameColumn(DatabaseConnectionInfo databaseConnectionInfo, RenameColumnRequestDTO ddlRequestDTO) {
        String renameColumnSQL = ddlParser.commandToSql(ddlRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, renameColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName());
        return new RenameColumnDDLResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), createTableStatement);
    }
}
