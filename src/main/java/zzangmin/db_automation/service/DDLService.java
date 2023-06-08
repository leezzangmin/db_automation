package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.CreateTableRequestDTO;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.parser.DDLParser;

@Slf4j
@RequiredArgsConstructor
@Service
public class DDLService {

    private final DDLParser ddlParser;
    private final MysqlClient mysqlClient;

    public void validate(String dbName, String ddlCommand) {

    }

    public String createTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {
        String createTableSQL = ddlParser.commandToSql(createTableRequestDTO);
        return mysqlClient.executeSQL(databaseConnectionInfo, createTableSQL);
    }

}
