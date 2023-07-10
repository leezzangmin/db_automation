package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.convention.TableConvention;
import zzangmin.db_automation.entity.Table;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.AwsService;
import zzangmin.db_automation.service.DescribeService;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SchemaStandardChecker {

    private final AwsService awsService;
    private final MysqlClient mysqlClient;
    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final TableConvention tableConvention;

    // 스키마, 계정 권한 등
    public String checkSchemaStandard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        List<DatabaseConnectionInfo> databaseConnectionInfos = awsService.findAllInstanceInfo().stream()
                .map(cluster -> cluster.getValueForField("DBInstanceIdentifier", String.class).get())
                .map(clusterName -> dynamicDataSourceProperties.findByDbName(clusterName))
                .collect(Collectors.toList());

        for (DatabaseConnectionInfo databaseConnectionInfo : databaseConnectionInfos) {
            List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                    .stream()
                    .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                    .collect(Collectors.toList());
            for (String schemaName : schemaNames) {
                List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
                List<Table> tables = mysqlClient.findTables(databaseConnectionInfo, schemaName, tableNames);
                for (Table table : tables) {
                    try {
                        tableConvention.validateTableConvention(table);
                    } catch (Exception e) {
                        sb.append(String.format("Cluster Name: %s, 스키마명: %s, 테이블명: %s 오류: [", databaseConnectionInfo.getDatabaseName(), schemaName, table.getTableName()));
                        sb.append(e.getMessage());
                        sb.append("]\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}
