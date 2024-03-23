package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Table;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SchemaObjectService;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TableDifferenceChecker {

    private final MysqlClient mysqlClient;
    private final SchemaObjectService schemaObjectService;

    public String compareTableSchema(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();

        for (String schemaName : schemaNames) {
            List<String> sourceTableNames = mysqlClient.findTableNames(sourceInfo, schemaName);
            Map<String, Table> sourceTables = mysqlClient.findTables(sourceInfo, schemaName, sourceTableNames)
                    .stream()
                    .collect(Collectors.toMap(
                            table -> table.getTableName(),
                            table -> table));
            Map<String, Table> replicaTables = mysqlClient.findTables(replicaInfo, schemaName, sourceTableNames)
                    .stream()
                    .collect(Collectors.toMap(
                            table -> table.getTableName(),
                            table -> table));

            for (String sourceTableName : sourceTableNames) {
                Table sourceTable = sourceTables.get(sourceTableName);
                Table replicaTable = replicaTables.getOrDefault(sourceTableName, null);
                differenceResult.append(sourceTable.reportDifference(replicaTable));
            }
        }

        log.info("TableDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveTable(DatabaseConnectionInfo databaseConnectionInfo) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("serviceName: {}", serviceName);
        List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toList());
        for (String schemaName : schemaNames) {
            List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
            List<Table> tables = mysqlClient.findTables(databaseConnectionInfo, schemaName, tableNames);
            log.info("save tables: {}", tables);
            schemaObjectService.saveTables(serviceName, schemaName, tables);
        }
    }

}
