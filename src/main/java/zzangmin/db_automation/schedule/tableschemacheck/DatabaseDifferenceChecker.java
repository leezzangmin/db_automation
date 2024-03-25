package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.SchemaObjectService;
import zzangmin.db_automation.util.StringMessageUtil;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class DatabaseDifferenceChecker {

    private final MysqlClient mysqlClient;
    private final SchemaObjectService schemaObjectService;

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

    public String compareDatabaseCrossAccount(DatabaseConnectionInfo databaseConnectionInfo) {
        StringBuilder differenceResult = new StringBuilder();
        String serviceName = databaseConnectionInfo.findServiceName();
        Map<String, String> prodDatabases = schemaObjectService.findDatabases(serviceName);
        log.info("prodDatabases: {}, \nprodDatabaseKeys: {}", prodDatabases, prodDatabases.keySet());
        Map<String, String> currentDatabases = mysqlClient.findSchemaNames(databaseConnectionInfo)
                .stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(databaseConnectionInfo, schemaName).get()));
        log.info("currentDatabases: {} \ncurrentDatabaseKeys: {}", currentDatabases, currentDatabases.keySet());

        for (String prodSchemaName : prodDatabases.keySet()) {
            String prodStatement = prodDatabases.get(prodSchemaName);
            String currentStatement = currentDatabases.get(prodSchemaName);
            if (currentStatement == null) {
                differenceResult.append(prodSchemaName + " DB가 존재하지 않습니다.");
            }
            else if (!prodStatement.equals(currentStatement)) {
                differenceResult.append(StringMessageUtil.convertCreateDatabaseDifferenceMessage(prodSchemaName, prodStatement, currentStatement));
            }
        }

        log.info("DatabaseDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveDatabase(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("serviceName: {}", serviceName);
        Map<String, String> schemaCreateStatements = schemaNames.stream()
                .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                .collect(Collectors.toMap(
                        schemaName -> schemaName,
                        schemaName -> mysqlClient.findCreateDatabaseStatement(databaseConnectionInfo, schemaName).get()));
        log.info("schemaCreateStatements: {}", schemaCreateStatements);
        schemaObjectService.saveDatabases(serviceName, schemaCreateStatements);
    }


}
