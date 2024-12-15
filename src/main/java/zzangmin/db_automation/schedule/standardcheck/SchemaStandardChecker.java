package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.convention.TableConvention;
import zzangmin.db_automation.dto.response.check.StandardCheckResultResponseDTO;
import zzangmin.db_automation.entity.mysqlobject.Table;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SchemaStandardChecker {

    private final MysqlClient mysqlClient;

    public List<StandardCheckResultResponseDTO> checkSchemaStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

        List<DatabaseConnectionInfo> databaseConnectionInfos = DynamicDataSourceProperties.findAllDatabases().values().stream().toList();

        for (DatabaseConnectionInfo databaseConnectionInfo : databaseConnectionInfos) {
            List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                    .stream()
                    .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                    .toList();
            for (String schemaName : schemaNames) {
                List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
                List<Table> tables = mysqlClient.findTables(databaseConnectionInfo, schemaName, tableNames);
                for (Table table : tables) {
                    List<String> errors = TableConvention.validateTableConvention(table);
                    for (String error : errors) {
                        results.add(new StandardCheckResultResponseDTO(databaseConnectionInfo.getAccountId(),
                                databaseConnectionInfo.getDatabaseName(),
                                StandardCheckResultResponseDTO.StandardType.SCHEMA,
                                "테이블 표준: " + table.getTableName(), null, null, error));
                    }
                }
            }
        }
        return results;
    }
}
