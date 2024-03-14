package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Procedure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProcedureDifferenceChecker {

    private final MysqlClient mysqlClient;

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

        return differenceResult.toString();
    }
}
