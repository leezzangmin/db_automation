package zzangmin.db_automation.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.QueriesInLongTransactionResponseDTO;
import zzangmin.db_automation.entity.Query;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class LongTransactionDetector {

    private final long HISTORY_LIST_LENGTH_THRESHOLD = 100L;
    private final long TRANSACTION_SEARCH_DELAY = 15000L;

    private final MysqlClient mysqlClient;
    private final SlackClient slackClient;

    @Scheduled(fixedDelay = TRANSACTION_SEARCH_DELAY)
    public void findLongTransaction() {
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.getDatabases();
        for (String databaseName : databases.keySet()) {
            long historyListLength = mysqlClient.findHistoryListLength(databases.get(databaseName));
            if (historyListLength >= HISTORY_LIST_LENGTH_THRESHOLD) {
                log.warn("{} 의 HLL이 {} 입니다. 임계치: {}", databaseName, historyListLength, HISTORY_LIST_LENGTH_THRESHOLD);
                int processlistId = mysqlClient.findLongTransactionProcesslistId(databases.get(databaseName));
                List<Query> queries = mysqlClient.findQueryInTransaction(databases.get(databaseName), processlistId);
                slackClient.sendMessage(new QueriesInLongTransactionResponseDTO(databaseName, historyListLength, queries).toString());
            }
        }
    }
}
