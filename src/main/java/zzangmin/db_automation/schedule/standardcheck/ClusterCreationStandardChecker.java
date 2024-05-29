package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBCluster;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.ClusterCreationStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ClusterCreationStandardChecker {

    private final AwsService awsService;

    public String checkClusterCreationStandard() {
        StringBuilder sb = new StringBuilder();
        Map<String, List<DBCluster>> dbClusters = awsService.findAllClusterInfo();
        for (String accountId : dbClusters.keySet()) {
            List<DBCluster> accountDbClusters = dbClusters.get(accountId);
            for (DBCluster dbCluster : accountDbClusters) {
                Set<String> creationStandardNames = ClusterCreationStandard.clusterCreationStandard.keySet();
                for (String creationStandardName : creationStandardNames) {
                    String value = String.valueOf(dbCluster.getValueForField(creationStandardName, Object.class)
                            .orElseThrow(() -> new IllegalArgumentException("해당 필드가 존재하지 않습니다." + creationStandardName)));
                    if (!ClusterCreationStandard.findStandardValue(creationStandardName).equals(value)) {
                        sb.append(String.format("\naccountId: %s, Cluster Name: %s, 비표준 설정명: %s, 표준값: %s, 현재값: %s", accountId, dbCluster.dbClusterIdentifier(), creationStandardName, ClusterCreationStandard.findStandardValue(creationStandardName), value));
                    }
                }
            }
        }

        return sb.toString();
    }


}
