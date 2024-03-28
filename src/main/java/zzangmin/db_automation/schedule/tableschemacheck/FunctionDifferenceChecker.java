package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Function;
import zzangmin.db_automation.service.SchemaObjectService;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class FunctionDifferenceChecker {

    private final MysqlClient mysqlClient;
    private final SchemaObjectService schemaObjectService;

    public String compareFunction(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();

        for (String schemaName : schemaNames) {

            Map<String, Function> sourceFunctions = mysqlClient.findFunctions(sourceInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            function -> function.getFunctionName(),
                            function -> function
                    ));

            Map<String, Function> replicaFunctions = mysqlClient.findFunctions(replicaInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            function -> function.getFunctionName(),
                            function -> function
                    ));

            for (String sourceFunctionName : sourceFunctions.keySet()) {
                Function sourceFunction = sourceFunctions.get(sourceFunctionName);
                Function replicaFunction = replicaFunctions.getOrDefault(sourceFunctionName, null);
                differenceResult.append(sourceFunction.reportDifference(replicaFunction));
            }

        }

        log.info("FunctionDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveFunctions(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("serviceName: {}", serviceName);

        for (String schemaName : schemaNames) {
            List<Function> functions = mysqlClient.findFunctions(databaseConnectionInfo, schemaName);

            log.info("save functions: {}", functions);
            schemaObjectService.saveFunctions(serviceName, schemaName, functions);
        }
    }

    public String compareFunctionCrossAccount(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        StringBuilder differenceResult = new StringBuilder();
        log.info("compareFunctionCrossAccount database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("compareFunctionCrossAccount serviceName: {}", serviceName);

        Map<String, Function> prodFunctions = schemaObjectService.findFunctions(serviceName, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        function -> function.getFunctionName(),
                        function -> function));
        Map<String, Function> currentFunctions = mysqlClient.findFunctions(databaseConnectionInfo, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        function -> function.getFunctionName(),
                        function -> function));

        for (String prodFunctionName : prodFunctions.keySet()) {
            Function prodFunction = prodFunctions.get(prodFunctionName);
            Function currentFunction = currentFunctions.getOrDefault(prodFunctionName, null);
            differenceResult.append(prodFunction.reportDifference(currentFunction));
        }

        log.info("compareFunctionCrossAccount Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }
}
