package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.SchemaObjectService;
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
                differenceResult.append(sourceSchemaName);
                differenceResult.append("의 데이터베이스 생성문이 다릅니다.\nprod: ");
                differenceResult.append(sourceStatement);
                differenceResult.append("\nstage: ");
                differenceResult.append(replicaStatement);
                differenceResult.append("\n");
            }
        }

        log.info("DatabaseDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public String compareDatabaseCrossAccount(DatabaseConnectionInfo databaseConnectionInfo) {
        StringBuilder differenceResult = new StringBuilder();
        String serviceName = databaseConnectionInfo.getServiceName();
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
                differenceResult.append(prodSchemaName).append(" DB가 존재하지 않습니다.");
            }
            else if (!prodStatement.equals(currentStatement)) {
                differenceResult.append(prodSchemaName);
                differenceResult.append("의 데이터베이스 생성문이 다릅니다.\nprod: ");
                differenceResult.append(prodStatement);
                differenceResult.append("\nstage: ");
                differenceResult.append(currentStatement);
                differenceResult.append("\n");
            }
        }

        log.info("DatabaseDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveDatabase(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("saveDatabase: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.getServiceName();
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
