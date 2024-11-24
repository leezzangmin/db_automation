package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.mysqlobject.Table;
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

    public void saveTable(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.getServiceName();
        log.info("serviceName: {}", serviceName);

        for (String schemaName : schemaNames) {
            List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
            List<Table> tables = mysqlClient.findTables(databaseConnectionInfo, schemaName, tableNames);
            log.info("save tables: {}", tables);
            schemaObjectService.saveTables(serviceName, schemaName, tables);
        }
    }

    public String compareTableCrossAccount(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        StringBuilder differenceResult = new StringBuilder();
        log.info("compareTableCrossAccount database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.getServiceName();
        log.info("compareTableCrossAccount serviceName: {}", serviceName);
        List<String> currentTableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);

        Map<String, Table> prodTables = schemaObjectService.findTables(serviceName, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        table -> table.getTableName(),
                        table -> table));
        Map<String, Table> currentTables = mysqlClient.findTables(databaseConnectionInfo, schemaName, currentTableNames)
                .stream()
                .collect(Collectors.toMap(
                        table -> table.getTableName(),
                        table -> table));
        for (String prodTableName : prodTables.keySet()) {
            Table sourceTable = prodTables.get(prodTableName);
            Table replicaTable = currentTables.getOrDefault(prodTableName, null);
            differenceResult.append(sourceTable.reportDifference(replicaTable));
        }

        log.info("TableDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

}
