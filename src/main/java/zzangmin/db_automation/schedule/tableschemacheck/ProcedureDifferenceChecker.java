package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Procedure;
import zzangmin.db_automation.service.SchemaObjectService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProcedureDifferenceChecker {

    private final MysqlClient mysqlClient;
    private final SchemaObjectService schemaObjectService;

    public String compareProcedure(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();

        for (String schemaName : schemaNames) {

            Map<String, Procedure> sourceProcedures = mysqlClient.findProcedures(sourceInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            procedure -> procedure.getProcedureName(),
                            procedure -> procedure
                    ));

            Map<String, Procedure> replicaProcedures = mysqlClient.findProcedures(replicaInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            procedure -> procedure.getProcedureName(),
                            procedure -> procedure
                    ));

            for (String sourceProcedureName : sourceProcedures.keySet()) {
                Procedure sourceProcedure = sourceProcedures.get(sourceProcedureName);
                Procedure replicaProcedure = replicaProcedures.getOrDefault(sourceProcedureName, null);
                differenceResult.append(sourceProcedure.reportDifference(replicaProcedure));
            }
        }
        log.info("Procedure Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveProcedures(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("serviceName: {}", serviceName);

        for (String schemaName : schemaNames) {
            List<Procedure> procedures = mysqlClient.findProcedures(databaseConnectionInfo, schemaName);

            log.info("save procedures: {}", procedures);
            schemaObjectService.saveProcedures(serviceName, schemaName, procedures);
        }
    }

    public String compareProcedureCrossAccount(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        StringBuilder differenceResult = new StringBuilder();
        log.info("compareProcedureCrossAccount database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("compareProcedureCrossAccount serviceName: {}", serviceName);

        Map<String, Procedure> prodProcedures = schemaObjectService.findProcedures(serviceName, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        procedure -> procedure.getProcedureName(),
                        procedure -> procedure));
        Map<String, Procedure> currentProcedures = mysqlClient.findProcedures(databaseConnectionInfo, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        procedure -> procedure.getProcedureName(),
                        procedure -> procedure));

        for (String prodProcedureName : prodProcedures.keySet()) {
            Procedure prodProcedure = prodProcedures.get(prodProcedureName);
            Procedure currentProcedure = currentProcedures.getOrDefault(prodProcedureName, null);
            differenceResult.append(prodProcedure.reportDifference(currentProcedure));
        }

        log.info("compareProcedureCrossAccount Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }
}
