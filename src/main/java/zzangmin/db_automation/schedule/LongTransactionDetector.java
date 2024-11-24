package zzangmin.db_automation.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.QueriesInLongTransactionResponseDTO;
import zzangmin.db_automation.entity.mysqlobject.Query;
import zzangmin.db_automation.service.SlackService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class LongTransactionDetector {

    private final long HISTORY_LIST_LENGTH_THRESHOLD = 100L;
    private final long TRANSACTION_SEARCH_DELAY_MS = 15000L;

    private final MysqlClient mysqlClient;
    private final SlackService slackService;

    //@Scheduled(fixedDelay = TRANSACTION_SEARCH_DELAY_MS)
    public void findLongTransaction() {
        Map<String, DatabaseConnectionInfo> databases = DynamicDataSourceProperties.findAllDatabases();
        for (String databaseName : databases.keySet()) {
            long historyListLength = mysqlClient.findHistoryListLength(databases.get(databaseName));
            if (historyListLength >= HISTORY_LIST_LENGTH_THRESHOLD) {
                log.warn("{} 의 HLL이 {} 입니다. 임계치: {}", databaseName, historyListLength, HISTORY_LIST_LENGTH_THRESHOLD);
                int processlistId = mysqlClient.findLongTransactionProcesslistId(databases.get(databaseName));
                List<Query> queries = mysqlClient.findQueryInTransaction(databases.get(databaseName), processlistId);
                slackService.sendNormalStringMessage(new QueriesInLongTransactionResponseDTO(databaseName, historyListLength, queries).toString());
            }
        }
    }
}
