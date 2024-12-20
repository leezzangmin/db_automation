package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SlackMessageService;
import zzangmin.db_automation.util.ProfileUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
@Profile(value = "prod")
public class ProdSchemaMonitorImpl implements SchemaMonitor {

    private static final long SCHEMA_CHECK_DELAY = 999999999999999999L;

    private final SlackMessageService slackMessageService;
    private final MysqlClient mysqlClient;

    private final DatabaseDifferenceChecker databaseDifferenceChecker;
    private final TableDifferenceChecker tableDifferenceChecker;
    private final ViewDifferenceChecker viewDifferenceChecker;
    private final ProcedureDifferenceChecker procedureDifferenceChecker;
    private final TriggerDifferenceChecker triggerDifferenceChecker;
    private final FunctionDifferenceChecker functionDifferenceChecker;
    private final AccountDifferenceChecker accountDifferenceChecker;

    //@Scheduled(fixedDelay = SCHEMA_CHECK_DELAY)
    public void checkSchema() throws Exception {
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.findAllDatabases();
        for (String databaseName : databases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = databases.get(databaseName);
            List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                    .stream()
                    .filter(schemaName -> !DescribeService.schemaBlackList.contains(schemaName))
                    .collect(Collectors.toList());

            databaseDifferenceChecker.saveDatabase(databaseConnectionInfo, schemaNames);
            tableDifferenceChecker.saveTable(databaseConnectionInfo, schemaNames);
            viewDifferenceChecker.saveViews(databaseConnectionInfo, schemaNames);
            procedureDifferenceChecker.saveProcedures(databaseConnectionInfo, schemaNames);
            triggerDifferenceChecker.saveTriggers(databaseConnectionInfo, schemaNames);
            functionDifferenceChecker.saveFunctions(databaseConnectionInfo, schemaNames);
            accountDifferenceChecker.saveAccount(databaseConnectionInfo);
        }

        slackMessageService.sendNormalStringMessage(ProfileUtil.CURRENT_ENVIRONMENT_PROFILE + " 환경 schema 저장 완료 !\n");
    }

}
