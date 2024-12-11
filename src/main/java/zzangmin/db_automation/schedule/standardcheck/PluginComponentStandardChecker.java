package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.standardcheck.StandardCheckResultResponseDTO;
import zzangmin.db_automation.standardvalue.PluginComponentStandard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PluginComponentStandardChecker {

    private final MysqlClient mysqlClient;

    public List<StandardCheckResultResponseDTO>  checkPluginComponentStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

        List<DatabaseConnectionInfo> databaseConnectionInfos = DynamicDataSourceProperties.findAllDatabases()
                .values()
                .stream()
                .toList();
        List<String> essentialPluginComponentNames = PluginComponentStandard.essentialPluginComponentNames;
        List<String> standardVariableNames = PluginComponentStandard.getKeySet().stream().toList();

        for (DatabaseConnectionInfo databaseConnectionInfo : databaseConnectionInfos) {
            List<String> pluginComponentNames = mysqlClient.findInstalledPluginsAndComponentNames(databaseConnectionInfo);
            for (String essentialPluginComponentName : essentialPluginComponentNames) {
                if (!pluginComponentNames.contains(essentialPluginComponentName)) {
                    results.add(new StandardCheckResultResponseDTO(databaseConnectionInfo.getAccountId(),
                            databaseConnectionInfo.getDatabaseName(),
                            StandardCheckResultResponseDTO.StandardType.PLUGIN_COMPONENT,
                            essentialPluginComponentName,
                            null, null, "플러그인(컴포넌트)이 설치/활성화되지 않았습니다."));

                }
            }

            Map<String, String> variables = mysqlClient.findGlobalVariables(databaseConnectionInfo, standardVariableNames);
            for (String standardVariableName : standardVariableNames) {
                String findValue = null;
                try {
                    findValue = variables.get(standardVariableName);
                } catch (Exception e) {
                    results.add(new StandardCheckResultResponseDTO(databaseConnectionInfo.getAccountId(),
                            databaseConnectionInfo.getDatabaseName(),
                            StandardCheckResultResponseDTO.StandardType.PLUGIN_COMPONENT,
                            standardVariableName,
                            PluginComponentStandard.findStandardValue(standardVariableName), null, "플러그인(컴포넌트)의 설정값이 존재하지 않습니다."));
                }
                String standardValue = PluginComponentStandard.findStandardValue(standardVariableName);
                if (findValue != null && !findValue.equals(standardValue)) {
                    results.add(new StandardCheckResultResponseDTO(databaseConnectionInfo.getAccountId(),
                            databaseConnectionInfo.getDatabaseName(),
                            StandardCheckResultResponseDTO.StandardType.PLUGIN_COMPONENT,
                            standardVariableName,
                            PluginComponentStandard.findStandardValue(standardVariableName), findValue, "플러그인(컴포넌트)의 설정값이 다릅니다."));

                }
            }
        }
        return results;
    }

}
