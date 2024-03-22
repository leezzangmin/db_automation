package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.SchemaService;
import zzangmin.db_automation.util.StringMessageUtil;
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
    private final SchemaService schemaService;

    public String compareDatabase(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo) {
        StringBuilder differenceResult = new StringBuilder();

        Map<String, String> sourceSchemaCreateStatements = mysqlClient.findSchemaNames(sourceInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(sourceInfo, schemaName).get()));

        Map<String, String> stageSchemaCreateStatements = mysqlClient.findSchemaNames(replicaInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(replicaInfo, schemaName).get()));

        for (String sourceSchemaName : sourceSchemaCreateStatements.keySet()) {
            String sourceStatement = sourceSchemaCreateStatements.get(sourceSchemaName);
            String replicaStatement = stageSchemaCreateStatements.get(sourceSchemaName);

            if (!sourceStatement.equals(replicaStatement)) {
                differenceResult.append(StringMessageUtil.convertCreateDatabaseDifferenceMessage(sourceSchemaName, sourceStatement, replicaStatement));
            }
        }

        log.info("DatabaseDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveDatabase(DatabaseConnectionInfo databaseConnectionInfo) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("serviceName: {}", serviceName);
        Map<String, String> schemaCreateStatements = mysqlClient.findSchemaNames(databaseConnectionInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(databaseConnectionInfo, schemaName).get()));
        log.info("schemaCreateStatements: {}", schemaCreateStatements);
        schemaService.saveDatabases(serviceName, schemaCreateStatements);
    }


}
