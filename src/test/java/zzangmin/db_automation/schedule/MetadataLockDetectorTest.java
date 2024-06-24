package zzangmin.db_automation.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.entity.MetadataLockHolder;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class MetadataLockDetectorTest {

    @Mock
    private MysqlClient mysqlClient;

    private MetadataLockDetector metadataLockDetector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        metadataLockDetector = new MetadataLockDetector(mysqlClient);
    }

    @DisplayName("MetadataLockDetector 가 targetDatabases 가 비어있을 때, findMetadataLockHolders 와 killSession 을 호출하지 않는다.")
    @Test
    void checkMetadataLock_NoTargetDatabases() {
        // given
        // when
        metadataLockDetector.checkMetadataLock();
        // then
        verify(mysqlClient, never()).findMetadataLockHolders(any());
        verify(mysqlClient, never()).killSession(any(), anyLong());
    }

    @DisplayName("MetadataLockDetector 가 targetDatabases 가 비어있지 않을 때, findMetadataLockHolders 와 killSession을 (3초이상 holder) 호출한다")
    @Test
    void checkMetadataLock_WithTargetDatabases() {
        // given

        DatabaseConnectionInfo databaseConnectionInfo = new DatabaseConnectionInfo("prod", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);
        List<MetadataLockHolder> metadataLockHolders = new ArrayList<>();
        metadataLockHolders.add(new MetadataLockHolder(null, null, null, null, null, 1, 1, null, 3));
        metadataLockHolders.add(new MetadataLockHolder(null, null, null, null, null, 2, 2, null, 2));
        when(mysqlClient.findMetadataLockHolders(databaseConnectionInfo)).thenReturn(metadataLockHolders);

        // when
        metadataLockDetector.checkMetadataLock();

        // then
        verify(mysqlClient).findMetadataLockHolders(databaseConnectionInfo);
        verify(mysqlClient).killSession(databaseConnectionInfo, 1);
        verify(mysqlClient, never()).killSession(databaseConnectionInfo, 2);
    }

    @DisplayName("MetadataLockDetector 가 endCheck 를 호출하면 targetDatabases 에서 목록이 제거된다")
    @Test
    void endCheck_RemovesTargetDatabase() throws Exception {
        // given
        DatabaseConnectionInfo databaseConnectionInfo = new DatabaseConnectionInfo("prod", "123", "shop", DatabaseConnectionInfo.DatabaseType.CLUSTER, "dbname", "admin", "com.mysql.cj.jdbc.Driver", "123*", null);

        metadataLockDetector.startCheck(databaseConnectionInfo);

        // when
        metadataLockDetector.endCheck(databaseConnectionInfo);

        // then (리플렉션)
        Field targetDatabasesField = MetadataLockDetector.class.getDeclaredField("targetDatabases");
        targetDatabasesField.setAccessible(true);
        Map<String, DatabaseConnectionInfo> targetDatabases = (Map<String, DatabaseConnectionInfo>) targetDatabasesField.get(metadataLockDetector);
        assertTrue(targetDatabases.isEmpty());
    }
}
