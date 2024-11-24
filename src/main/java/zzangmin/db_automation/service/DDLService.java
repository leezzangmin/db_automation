package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.ddl.*;
import zzangmin.db_automation.entity.mysqlobject.Column;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

@Slf4j
@RequiredArgsConstructor
@Service
public class DDLService {

    private final MysqlClient mysqlClient;

    public String createTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        String createTableSQL = createTableRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, createTableSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        return createTableStatement;
    }

    public String createIndex(DatabaseConnectionInfo databaseConnectionInfo, CreateIndexRequestDTO createIndexRequestDTO) {
        String createIndexSQL = createIndexRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, createIndexSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        return createTableStatement;
    }

    public String addColumn(DatabaseConnectionInfo databaseConnectionInfo, AddColumnRequestDTO addColumnRequestDTO) {
        String addColumnSQL = addColumnRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, addColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, addColumnRequestDTO.getSchemaName(), addColumnRequestDTO.getTableName());
        return createTableStatement;
    }

    public String deleteColumn(DatabaseConnectionInfo databaseConnectionInfo, DeleteColumnRequestDTO deleteColumnRequestDTO) {
        String deleteColumnSQL = deleteColumnRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, deleteColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, deleteColumnRequestDTO.getSchemaName(), deleteColumnRequestDTO.getTableName());
        return createTableStatement;
    }

    public String alterColumn(DatabaseConnectionInfo databaseConnectionInfo, AlterColumnRequestDTO alterColumnRequestDTO) {
        String alterColumnSQL = alterColumnRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, alterColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, alterColumnRequestDTO.getSchemaName(), alterColumnRequestDTO.getTableName());
        return createTableStatement;
    }

    public String renameColumn(DatabaseConnectionInfo databaseConnectionInfo, RenameColumnRequestDTO renameColumnRequestDTO) {
        String renameColumnSQL = renameColumnRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, renameColumnSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo,
                renameColumnRequestDTO.getSchemaName(),
                renameColumnRequestDTO.getTableName());
        return createTableStatement;
    }

    public String extendVarcharColumn(DatabaseConnectionInfo databaseConnectionInfo,
                                                                 ExtendVarcharColumnRequestDTO extendVarcharColumnRequestDTO) {
        Column column = mysqlClient.findColumn(databaseConnectionInfo,
                        extendVarcharColumnRequestDTO.getSchemaName(),
                        extendVarcharColumnRequestDTO.getTableName(),
                        extendVarcharColumnRequestDTO.getOldColumn().getName())
                .orElseThrow(() -> new IllegalArgumentException("컬럼 정보를 불러올 수 없습니다. 존재하지 않는 컬럼명: "+ extendVarcharColumnRequestDTO.getOldColumn().getName()));
        String extendVarcharSQL = extendVarcharColumnRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, extendVarcharSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, extendVarcharColumnRequestDTO.getSchemaName(), extendVarcharColumnRequestDTO.getTableName());
        return createTableStatement;
    }

    public String renameTable(DatabaseConnectionInfo databaseConnectionInfo,RenameTableRequestDTO renameTableRequestDTO) {
        String renameTableSQL = renameTableRequestDTO.toSQL();
        mysqlClient.executeSQL(databaseConnectionInfo, renameTableSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo,
                renameTableRequestDTO.getSchemaName(),
                renameTableRequestDTO.getNewTableName());
        return createTableStatement;
    }
}
