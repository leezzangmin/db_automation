package zzangmin.db_automation.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.services.rds.model.DBCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RdsClustersResponseDTO {

    private List<RdsClusterResponseDTO> clusters = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    public static class RdsClusterResponseDTO {
        private String clusterName;
        private String engineVersion;
        private boolean multiAzEnabled;
        private boolean deletionProtectionEnabled;
        private int cpuUsage;
        private long freeableMemory;
        private int averageActiveSession;
        private int connection;
        private long freeLocalStorage;
        private long selectThroughput;
        private long writeThroughput;

        public static RdsClusterResponseDTO of(DBCluster dbCluster, Map<String, Long> metrics) {

            String clusterName = dbCluster.getValueForField("DBClusterIdentifier", String.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBClusterDTO 파싱 오류"));
            String engineVersion = dbCluster.getValueForField("EngineVersion", String.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBClusterDTO 파싱 오류"));
            boolean multiAzEnabled = dbCluster.getValueForField("MultiAZ", Boolean.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBClusterDTO 파싱 오류"));
            boolean deletionProtectionEnabled = dbCluster.getValueForField("DeletionProtection", Boolean.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBClusterDTO 파싱 오류"));

            int cpuUsage = metrics.get("cpuUsage").intValue();
            long memoryUsage = metrics.get("freeableMemory");
            int averageActiveSession = metrics.get("averageActiveSession").intValue();
            int connection = metrics.get("connection").intValue();
            long diskUsage = metrics.get("freeLocalStorage");
            long selectThroughput = metrics.get("readThroughput");
            long writeThroughput = metrics.get("writeThroughput");

            return new RdsClusterResponseDTO(clusterName, engineVersion, multiAzEnabled, deletionProtectionEnabled,
                    cpuUsage, memoryUsage, averageActiveSession, connection, diskUsage, selectThroughput, writeThroughput);
        }
    }

    public static RdsClustersResponseDTO of(List<DBCluster> dbClusters, List<Map<String, Long>> metricsList) {
        List<RdsClusterResponseDTO> clusters = new ArrayList<>();
        for (int i = 0; i < dbClusters.size(); i++) {
            clusters.add(RdsClusterResponseDTO.of(dbClusters.get(i), metricsList.get(i)));
        }
        return new RdsClustersResponseDTO(clusters);
    }

}
