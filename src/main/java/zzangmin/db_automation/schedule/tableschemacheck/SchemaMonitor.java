package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class SchemaMonitor {

    private static final long SCHEMA_CHECK_DELAY = 50000l;

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final MysqlClient mysqlClient;
    private final SlackClient slackClient;

    private final DatabaseDifferenceChecker databaseDifferenceChecker;
    private final TableDifferenceChecker tableDifferenceChecker;
    private final ViewDifferenceChecker viewDifferenceChecker;
    private final ProcedureDifferenceChecker procedureDifferenceChecker;
    private final TriggerDifferenceChecker triggerDifferenceChecker;
    private final FunctionDifferenceChecker functionDifferenceChecker;


    @Scheduled(fixedDelay = SCHEMA_CHECK_DELAY)
    public void checkSchema() {
        Map<String, DatabaseConnectionInfo> databases = dynamicDataSourceProperties.getDatabases();
        for (String databaseName : databases.keySet()) {

        }

    }
}
