package zzangmin.db_automation.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.QueriesInLongTransactionResponseDTO;
import zzangmin.db_automation.entity.mysqlobject.Query;
import zzangmin.db_automation.service.SlackMessageService;
import zzangmin.db_automation.standardvalue.LongQueryStandard;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class LongTransactionDetector {

    private final long HISTORY_LIST_LENGTH_THRESHOLD = 100L;
    private final long TRANSACTION_SEARCH_DELAY_MS = 150000L;

    private final MysqlClient mysqlClient;
    private final SlackMessageService slackMessageService;

//    @Scheduled(fixedDelay = TRANSACTION_SEARCH_DELAY_MS)
    public void findLongTransaction() {
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.findAllDatabases();
        for (String databaseName : databases.keySet()) {
            checkHistoryListLength(databases.get(databaseName));
            checkLongTransaction(databases.get(databaseName));
        }
    }

    private void checkHistoryListLength(DatabaseConnectionInfo databaseConnectionInfo) {
        long historyListLength = mysqlClient.findHistoryListLength(databaseConnectionInfo);
        if (historyListLength >= HISTORY_LIST_LENGTH_THRESHOLD) {
            log.warn("{} 의 HLL이 {} 입니다. 임계치: {}", databaseConnectionInfo.getDatabaseName(), historyListLength, HISTORY_LIST_LENGTH_THRESHOLD);
            int processlistId = mysqlClient.findLongTransactionProcesslistId(databaseConnectionInfo, LongQueryStandard.LONG_SESSION_SECONDS_THRESHOLD);
            List<Query> queries = mysqlClient.findQueryInTransaction(databaseConnectionInfo, processlistId);
            slackMessageService.sendNormalStringMessage(new QueriesInLongTransactionResponseDTO(databaseConnectionInfo.getDatabaseName(), historyListLength, queries).toString());
        }
    }

    private void checkLongTransaction(DatabaseConnectionInfo databaseConnectionInfo) {
        int processlistId = mysqlClient.findLongTransactionProcesslistId(databaseConnectionInfo, LongQueryStandard.LONG_SESSION_SECONDS_THRESHOLD);
        List<Query> queries = mysqlClient.findQueryInTransaction(databaseConnectionInfo, processlistId);
        for (Query query : queries) {
            System.out.println("query = " + query);
        }
        slackMessageService.sendNormalStringMessage(new QueriesInLongTransactionResponseDTO(databaseConnectionInfo.getDatabaseName(), null, queries).toString());
    }
}
