package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.StringMessageUtil;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;


import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class DatabaseDifferenceChecker {

    private final MysqlClient mysqlClient;


    public String compareDatabase(DatabaseConnectionInfo prodInfo, DatabaseConnectionInfo stageInfo) {
        StringBuilder differenceResult = new StringBuilder();

        Map<String, String> prodSchemaCreateStatements = mysqlClient.findSchemaNames(prodInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(prodInfo, schemaName).get()));

        Map<String, String> stageSchemaCreateStatements = mysqlClient.findSchemaNames(stageInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(stageInfo, schemaName).get()));

        for (String prodSchemaName : prodSchemaCreateStatements.keySet()) {
            String prodStatement = prodSchemaCreateStatements.get(prodSchemaName);
            String stageStatement = stageSchemaCreateStatements.get(prodSchemaName);

            if (!prodStatement.equals(stageStatement)) {
                differenceResult.append(StringMessageUtil.convertCreateDatabaseDifferenceMessage(prodSchemaName, prodStatement, stageStatement));
            }
        }

        return differenceResult.toString();
    }


}
