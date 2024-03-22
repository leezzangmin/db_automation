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

import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
@Profile(value = "!dev | !stage | prod")
public class ProdSchemaMonitorImpl {

    private static final long SCHEMA_CHECK_DELAY = 999999999999999999L;

    private final SlackClient slackClient;
    private final MysqlClient mysqlClient;

    private final DatabaseDifferenceChecker databaseDifferenceChecker;
    private final TableDifferenceChecker tableDifferenceChecker;
    private final ViewDifferenceChecker viewDifferenceChecker;
    private final ProcedureDifferenceChecker procedureDifferenceChecker;
    private final TriggerDifferenceChecker triggerDifferenceChecker;
    private final FunctionDifferenceChecker functionDifferenceChecker;


    @Scheduled(fixedDelay = SCHEMA_CHECK_DELAY)
    public void checkSchema() throws Exception {
        StringBuilder schemaSaveResult = new StringBuilder();

        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.getDatabases();
        for (String databaseName : databases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = databases.get(databaseName);
            databaseDifferenceChecker.saveDatabase(databaseConnectionInfo);
        }

        // slackClient.sendMessage(schemaSaveResult.toString());
    }

}
