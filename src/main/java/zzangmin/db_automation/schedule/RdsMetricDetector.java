package zzangmin.db_automation.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.entity.mysqlobject.MysqlProcess;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.validator.RdsMetricValidator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class RdsMetricDetector {

    private static final int CHECK_DELAY = 10000;
    private final RdsMetricValidator rdsMetricValidator;
    private final MysqlClient mysqlClient;

    private Map<String, DatabaseConnectionInfo> targetDatabases = new ConcurrentHashMap<>();

    public void startCheck(DatabaseConnectionInfo databaseConnectionInfo) {
        targetDatabases.put(databaseConnectionInfo.getDatabaseName(), databaseConnectionInfo);
    }

    public void endCheck(DatabaseConnectionInfo databaseConnectionInfo) {
        targetDatabases.remove(databaseConnectionInfo.getDatabaseName());
    }

    //@Scheduled(fixedDelay = CHECK_DELAY)
    public void checkRdsMetric() {
        if (targetDatabases.isEmpty()) {
            return;
        }

        for (String databaseName : targetDatabases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = targetDatabases.get(databaseName);
            try {
                rdsMetricValidator.validateMetricStable(databaseConnectionInfo.getAccountId(), databaseConnectionInfo.getDatabaseName());
            } catch (Exception e) {
                killDDLExecutingSession(databaseConnectionInfo);
                endCheck(databaseConnectionInfo);
            }
        }
    }

    private void killDDLExecutingSession(DatabaseConnectionInfo databaseConnectionInfo) {
        MysqlProcess ddlExecutingSession = mysqlClient.findDDLExecutingSession(databaseConnectionInfo)
                .orElseThrow(() -> new IllegalStateException("DDL 실행중인 세션이 없습니다."));
        mysqlClient.killSession(databaseConnectionInfo, ddlExecutingSession.getId());
    }
}
