package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.standardcheck.StandardCheckResultResponseDTO;
import zzangmin.db_automation.standardvalue.VariableStandard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class VariableStandardChecker {

    private final MysqlClient mysqlClient;

    public List<StandardCheckResultResponseDTO> checkVariableStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.findAllDatabases();
        for (String databaseName : databases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = databases.get(databaseName);
            results.addAll(checkVariableStandard(databaseConnectionInfo));
            results.addAll(checkValueStandard(databaseConnectionInfo));
        }
        return results;
    }

    private List<StandardCheckResultResponseDTO> checkVariableStandard(DatabaseConnectionInfo databaseConnectionInfo) {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

        Set<String> variableNames = VariableStandard.getVariableKeySet();
        Map<String, String> findVariables = mysqlClient.findGlobalVariables(databaseConnectionInfo, variableNames.stream().toList());
        for (String variableName : variableNames) {
            if (!findVariables.containsKey(variableName)) {
                results.add(new StandardCheckResultResponseDTO(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName(), StandardCheckResultResponseDTO.StandardType.VARIABLE, variableName, VariableStandard.findVariableStandardValue(variableName), null, "해당 variable을 DB에서 조회할 수 없습니다."));
                continue;
            }
            if (!findVariables.get(variableName).equals(VariableStandard.findVariableStandardValue(variableName))) {
                results.add(new StandardCheckResultResponseDTO(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName(), StandardCheckResultResponseDTO.StandardType.VARIABLE, variableName, VariableStandard.findVariableStandardValue(variableName), findVariables.get(variableName), "설정값이 표준과 다릅니다."));
            }
        }
        return results;
    }

    private List<StandardCheckResultResponseDTO> checkValueStandard(DatabaseConnectionInfo databaseConnectionInfo) {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();
        Set<String> valueSqls = VariableStandard.getValueKeySet();
        for (String valueSql : valueSqls) {
            String standardValue = VariableStandard.findValueStandardValue(valueSql);
            String findValue = mysqlClient.executeScalarSelectQuery(databaseConnectionInfo, valueSql)
                    .orElseGet(() -> {
                        results.add(new StandardCheckResultResponseDTO(
                                databaseConnectionInfo.getAccountId(),
                                databaseConnectionInfo.getDatabaseName(),
                                StandardCheckResultResponseDTO.StandardType.VARIABLE,
                                valueSql, VariableStandard.findValueStandardValue(valueSql), null, "해당 value를 DB에서 조회할 수 없습니다."));
                        return null;
                    });
            if (findValue != null && !standardValue.equals(findValue)) {
                results.add(new StandardCheckResultResponseDTO(
                        databaseConnectionInfo.getAccountId(),
                        databaseConnectionInfo.getDatabaseName(),
                        StandardCheckResultResponseDTO.StandardType.VARIABLE,
                        valueSql, VariableStandard.findValueStandardValue(valueSql), findValue, "해당 value를 DB에서 조회할 수 없습니다."));
            }

        }
        return results;
    }
}
