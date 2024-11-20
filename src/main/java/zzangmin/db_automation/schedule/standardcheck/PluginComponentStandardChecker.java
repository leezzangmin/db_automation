package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.standardvalue.PluginComponentStandard;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class PluginComponentStandardChecker {

    private final MysqlClient mysqlClient;

    public String checkPluginComponentStandard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        List<DatabaseConnectionInfo> databaseConnectionInfos = DynamicDataSourceProperties.findAllDatabases()
                .values()
                .stream()
                .collect(Collectors.toList());
        List<String> essentialPluginComponentNames = PluginComponentStandard.essentialPluginComponentNames;
        List<String> standardVariableNames = PluginComponentStandard.pluginComponentStandardValues.keySet()
                .stream()
                .collect(Collectors.toList());

        for (DatabaseConnectionInfo databaseConnectionInfo : databaseConnectionInfos) {
            List<String> pluginComponentNames = mysqlClient.findInstalledPluginsAndComponentNames(databaseConnectionInfo);
            for (String essentialPluginComponentName : essentialPluginComponentNames) {
                if (!pluginComponentNames.contains(essentialPluginComponentName)) {
                    sb.append(String.format("Cluster Name: %s 오류: [%s 플러그인(컴포넌트) 가 설치되어있지 않습니다.]\n", databaseConnectionInfo.getDatabaseName(), essentialPluginComponentName));
                }
            }

            Map<String, String> variables = mysqlClient.findGlobalVariables(databaseConnectionInfo, standardVariableNames);
            for (String standardVariableName : standardVariableNames) {
                String findValue = null;
                try {
                    findValue = variables.get(standardVariableName);
                } catch (Exception e) {
                    sb.append(String.format("Cluster Name: %s 오류: [%s 변수가 존재하지 않습니다.]\n", databaseConnectionInfo.getDatabaseName(), standardVariableName));
                }
                String standardValue = PluginComponentStandard.pluginComponentStandardValues.get(standardVariableName);
                if (findValue != null && findValue != standardValue) {
                    sb.append(String.format("Cluster Name: %s 오류: [%s 변수 값이 표준과 다릅니다.] 표준값: %s, 설정값: %s\n", databaseConnectionInfo.getDatabaseName(), standardVariableName, standardValue, findValue));
                }
            }

        }
        sb.append("\n");

        return sb.toString();
    }

}
