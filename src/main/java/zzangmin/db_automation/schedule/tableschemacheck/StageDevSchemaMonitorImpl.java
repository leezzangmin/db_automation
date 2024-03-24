package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static zzangmin.db_automation.service.DescribeService.schemaBlackList;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile(value = "!prod")
public class StageDevSchemaMonitorImpl implements SchemaMonitor {

    private static final long SCHEMA_CHECK_DELAY = 500000l;

    private final SlackClient slackClient;
    private final MysqlClient mysqlClient;

    private final DatabaseDifferenceChecker databaseDifferenceChecker;
    private final TableDifferenceChecker tableDifferenceChecker;
    private final ViewDifferenceChecker viewDifferenceChecker;
    private final ProcedureDifferenceChecker procedureDifferenceChecker;
    private final TriggerDifferenceChecker triggerDifferenceChecker;
    private final FunctionDifferenceChecker functionDifferenceChecker;


    @Scheduled(fixedDelay = SCHEMA_CHECK_DELAY)
    public void checkSchema() {
        StringBuilder schemaCheckResult = new StringBuilder();
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.getDatabases();
        for (String databaseName : databases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = databases.get(databaseName);
            schemaCheckResult.append(databaseDifferenceChecker.compareDatabaseCrossAccount(databaseConnectionInfo));
        }



//            schemaCheckResult.append(tableDifferenceChecker.compareTableSchema(prodDB, stageDB, schemaNames));
//            schemaCheckResult.append(viewDifferenceChecker.compareView(prodDB, stageDB, schemaNames));
//            schemaCheckResult.append(procedureDifferenceChecker.compareProcedure(prodDB, stageDB, schemaNames));
//            schemaCheckResult.append(triggerDifferenceChecker.compareTrigger(prodDB, stageDB, schemaNames));
//            schemaCheckResult.append(functionDifferenceChecker.compareFunction(prodDB, stageDB, schemaNames));

        slackClient.sendMessage(schemaCheckResult.toString());
    }
}
