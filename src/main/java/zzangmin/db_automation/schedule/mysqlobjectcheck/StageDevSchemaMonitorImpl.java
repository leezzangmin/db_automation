package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.util.ProfileUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
@Profile(value = "!prod")
public class StageDevSchemaMonitorImpl implements SchemaMonitor {

    private static final long SCHEMA_CHECK_DELAY = 9999999999l;

    private final SlackService slackService;
    private final MysqlClient mysqlClient;

    private final DatabaseDifferenceChecker databaseDifferenceChecker;
    private final TableDifferenceChecker tableDifferenceChecker;
    private final ViewDifferenceChecker viewDifferenceChecker;
    private final ProcedureDifferenceChecker procedureDifferenceChecker;
    private final TriggerDifferenceChecker triggerDifferenceChecker;
    private final FunctionDifferenceChecker functionDifferenceChecker;
    private final AccountDifferenceChecker accountDifferenceChecker;


    //@Scheduled(fixedDelay = SCHEMA_CHECK_DELAY)
    public void checkSchema() {
        slackService.sendNormalStringMessage(ProfileUtil.CURRENT_ENVIRONMENT_PROFILE + " 환경 schema 검사 시작 !");

        StringBuilder schemaCheckResult = new StringBuilder();
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.findAllDatabases();
        for (String databaseName : databases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = databases.get(databaseName);

            List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                    .stream()
                    .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                    .collect(Collectors.toList());
            schemaCheckResult.append(databaseDifferenceChecker.compareDatabaseCrossAccount(databaseConnectionInfo));
            StringBuilder schemaResult = new StringBuilder();
            for (String schemaName : schemaNames) {
                schemaResult.append(tableDifferenceChecker.compareTableCrossAccount(databaseConnectionInfo, schemaName));
                schemaResult.append(viewDifferenceChecker.compareViewCrossAccount(databaseConnectionInfo, schemaName));
                schemaResult.append(procedureDifferenceChecker.compareProcedureCrossAccount(databaseConnectionInfo, schemaName));
                schemaResult.append(triggerDifferenceChecker.compareTriggerCrossAccount(databaseConnectionInfo, schemaName));
                schemaResult.append(functionDifferenceChecker.compareFunctionCrossAccount(databaseConnectionInfo, schemaName));

                if (!schemaResult.isEmpty()) {
                    schemaCheckResult.append("\n====================");
                    schemaCheckResult.append(schemaName);
                    schemaCheckResult.append(" 검사결과 [prod] <-> [");
                    schemaCheckResult.append(ProfileUtil.CURRENT_ENVIRONMENT_PROFILE);
                    schemaCheckResult.append("]====================\n");
                    schemaCheckResult.append(schemaResult);
                }
            }
            schemaResult.append(accountDifferenceChecker.compareAccountCrossAccount(databaseConnectionInfo));

        }
        if (schemaCheckResult.isEmpty()) {
            slackService.sendNormalStringMessage(ProfileUtil.CURRENT_ENVIRONMENT_PROFILE + " 환경 schema 검사 종료 !");
            return;
        }
        slackService.sendNormalStringMessage(schemaCheckResult.toString());
    }
}
