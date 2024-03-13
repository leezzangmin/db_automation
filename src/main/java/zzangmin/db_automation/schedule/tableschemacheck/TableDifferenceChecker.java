package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Table;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TableDifferenceChecker {

    private final MysqlClient mysqlClient;

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


        return differenceResult.toString();
    }


}
