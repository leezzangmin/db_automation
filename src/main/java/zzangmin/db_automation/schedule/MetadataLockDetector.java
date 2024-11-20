package zzangmin.db_automation.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.entity.MetadataLockHolder;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.standardvalue.LongQueryStandard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
@Component
public class MetadataLockDetector {

    private Map<String, DatabaseConnectionInfo> targetDatabases = new ConcurrentHashMap<>();
    private final MysqlClient mysqlClient;

    public void startCheck(DatabaseConnectionInfo databaseConnectionInfo) {
        targetDatabases.put(databaseConnectionInfo.getDatabaseName(), databaseConnectionInfo);
    }

    public void endCheck(DatabaseConnectionInfo databaseConnectionInfo) {
        targetDatabases.remove(databaseConnectionInfo.getDatabaseName());
    }

    //@Scheduled(fixedDelay = METADATA_LOCK_CHECK_DELAY_MS)
    public void checkMetadataLock() {
        if (targetDatabases.isEmpty()) {
            return;
        }

        for (String databaseName : targetDatabases.keySet()) {
            DatabaseConnectionInfo databaseConnectionInfo = targetDatabases.get(databaseName);
            List<MetadataLockHolder> metadataLockHolders = mysqlClient.findMetadataLockHolders(databaseConnectionInfo);
            killLongMetadataLockHolder(databaseConnectionInfo, metadataLockHolders);
        }
    }

    private void killLongMetadataLockHolder(DatabaseConnectionInfo databaseConnectionInfo, List<MetadataLockHolder> metadataLockHolders) {
        for (MetadataLockHolder metadataLockHolder : metadataLockHolders) {
            if (metadataLockHolder.getProcesslistTime() >= LongQueryStandard.METADATA_LOCK_THRESHOLD_SECONDS) {
                long sessionId = metadataLockHolder.getProcesslistId();
                mysqlClient.killSession(databaseConnectionInfo, sessionId);
            }
        }
    }

}

