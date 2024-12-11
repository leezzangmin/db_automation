package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBCluster;
import zzangmin.db_automation.dto.response.standardcheck.StandardCheckResultResponseDTO;
import zzangmin.db_automation.standardvalue.ClusterCreationStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ClusterCreationStandardChecker {

    private final AwsService awsService;

    public List<StandardCheckResultResponseDTO> checkClusterCreationStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

        Map<String, List<DBCluster>> dbClusters = awsService.findAllClusterInfo();
        for (String accountId : dbClusters.keySet()) {
            List<DBCluster> accountDbClusters = dbClusters.get(accountId);
            for (DBCluster dbCluster : accountDbClusters) {
                Set<String> creationStandardNames = ClusterCreationStandard.getKeySet();
                for (String creationStandardName : creationStandardNames) {
                    String findValue = String.valueOf(dbCluster.getValueForField(creationStandardName, Object.class)
                            .orElseThrow(() -> new IllegalArgumentException("해당 필드가 존재하지 않습니다." + creationStandardName)));
                    if (!ClusterCreationStandard.findStandardValue(creationStandardName).equals(findValue)) {
                        results.add(new StandardCheckResultResponseDTO(accountId, dbCluster.dbClusterIdentifier(), StandardCheckResultResponseDTO.StandardType.CLUSTER_CREATION, creationStandardName, ClusterCreationStandard.findStandardValue(creationStandardName), findValue, "클러스터 생성 설정 값이 표준과 다릅니다."));
                    }
                }
            }
        }
        return results;
    }


}
