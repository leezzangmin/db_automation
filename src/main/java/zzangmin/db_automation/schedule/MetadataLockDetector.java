package zzangmin.db_automation.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.aop.ConcurrentDDLAspect;
import zzangmin.db_automation.client.MysqlClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RequiredArgsConstructor
@Component
public class MetadataLockDetector {

    private final ConcurrentDDLAspect concurrentDDLAspect;
    private final MysqlClient mysqlClient;

    private boolean ddlExecutionInProgress = false; // DDL 실행 중 여부를 나타내는 플래그

    public void setDdlExecutionInProgress(boolean ddlExecutionInProgress) {
        this.ddlExecutionInProgress = ddlExecutionInProgress;
    }

//    @Scheduled(fixedDelay = 100)
//    public void checkMetadataLock() {
//        if (!ddlExecutionInProgress) {
//            // DDL 실행 중이 아니면 메타데이터 락 감지 스케줄링 비활성화
//            return;
//        }
//
//        Connection connection = null;
//        Statement statement = null;
//        ResultSet resultSet = null;
//        try {
//            connection = mysqlClient.createConnection(); // MySQL 연결
//            statement = connection.createStatement();
//
//            resultSet = statement.executeQuery("SHOW PROCESSLIST");
//
//            while (resultSet.next()) {
//                String state = resultSet.getString("STATE");
//                if (state != null && state.contains("waiting for metadata lock")) {
//                    // 메타데이터 락이 감지된 경우 처리 로직 작성
//                    handleMetadataLockDetected();
//                    break; // 하나의 메타데이터 락만 감지하면 되므로 루프 종료
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            mysqlClient.closeResultSet(resultSet);
//            mysqlClient.closeStatement(statement);
//            mysqlClient.closeConnection(connection);
//        }
//    }

    private void handleMetadataLockDetected() {
        // 메타데이터 락이 감지된 경우 처리 로직 작성
    }
}

