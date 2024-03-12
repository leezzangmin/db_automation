package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Table;
import zzangmin.db_automation.service.DescribeService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TableDifferenceChecker {

    private final MysqlClient mysqlClient;

    public String compareTableSchema(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo) {
        StringBuilder differenceResult = new StringBuilder();

        List<String> sourceSchemaNames = mysqlClient.findSchemaNames(sourceInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toList());

        for (String sourceSchemaName : sourceSchemaNames) {
            List<String> sourceTableNames = mysqlClient.findTableNames(sourceInfo, sourceSchemaName);
            Map<String, Table> sourceTables = mysqlClient.findTables(sourceInfo, sourceSchemaName, sourceTableNames)
                    .stream()
                    .collect(Collectors.toMap(
                            table -> table.getTableName(),
                            table -> table));
            Map<String, Table> replicaTables = mysqlClient.findTables(replicaInfo, sourceSchemaName, sourceTableNames)
                    .stream()
                    .collect(Collectors.toMap(
                            table -> table.getTableName(),
                            table -> table));

            for (String sourceTableName : sourceTables.keySet()) {
                Table sourceTable = sourceTables.get(sourceTableName);
                Table replicaTable = replicaTables.get(sourceTableName);

                differenceResult.append(sourceTable.reportDifference(replicaTable));
            }
        }


        return differenceResult.toString();
    }


}
