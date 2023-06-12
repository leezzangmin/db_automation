package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.request.CreateIndexRequestDTO;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.dto.response.CreateIndexResponseDTO;
import zzangmin.db_automation.dto.response.CreateTableResponseDTO;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.parser.DDLParser;

@Slf4j
@RequiredArgsConstructor
@Service
public class DDLService {

    private final DDLParser ddlParser;
    private final MysqlClient mysqlClient;

    // TODO: AOP 메타락 조회 및 kill
    public CreateTableResponseDTO createTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        String createTableSQL = ddlParser.commandToSql(createTableRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, createTableSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName());
        return new CreateTableResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), createTableRequestDTO.getSchemaName(), createTableRequestDTO.getTableName(), createTableStatement);
    }

    public CreateIndexResponseDTO createIndex(DatabaseConnectionInfo databaseConnectionInfo, CreateIndexRequestDTO createIndexRequestDTO) {
        String createIndexSQL = ddlParser.commandToSql(createIndexRequestDTO);
        mysqlClient.executeSQL(databaseConnectionInfo, createIndexSQL);
        String createTableStatement = mysqlClient.findCreateTableStatement(databaseConnectionInfo, createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName());
        return new CreateIndexResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), createIndexRequestDTO.getSchemaName(), createIndexRequestDTO.getTableName(), createIndexRequestDTO.getIndexName(), createTableStatement);
    }
}
