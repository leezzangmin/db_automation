package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Function;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class FunctionDifferenceChecker {

    private final MysqlClient mysqlClient;

    public String compareFunction(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();

        for (String schemaName : schemaNames) {
            List<String> sourceFunctionNames = mysqlClient.findFunctionNames(sourceInfo, schemaName);

            Map<String, Function> sourceFunctions = mysqlClient.findFunctions(sourceInfo, schemaName, sourceFunctionNames)
                    .stream()
                    .collect(Collectors.toMap(
                            function -> function.getFunctionName(),
                            function -> function
                    ));

            Map<String, Function> replicaFunctions = mysqlClient.findFunctions(sourceInfo, schemaName, sourceFunctionNames)
                    .stream()
                    .collect(Collectors.toMap(
                            function -> function.getFunctionName(),
                            function -> function
                    ));

            for (String sourceFunctionName : sourceFunctionNames) {
                Function sourceFunction = sourceFunctions.get(sourceFunctionName);
                Function replicaFunction = replicaFunctions.getOrDefault(sourceFunctionName, null);
                differenceResult.append(sourceFunction.reportDifference(replicaFunction));
            }
        }

        return differenceResult.toString();
    }
}
