package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.Tag;
import zzangmin.db_automation.dto.response.standardcheck.StandardCheckResultResponseDTO;
import zzangmin.db_automation.standardvalue.TagStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class TagStandardChecker {

    private final AwsService awsService;

    public List<StandardCheckResultResponseDTO> checkTagStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

        List<String> standardTagKeyNames = TagStandard.standardTagKeyNames;
        Map<String, List<DBCluster>> dbClusters = awsService.findAllClusterInfo();
        for (String accountId : dbClusters.keySet()) {
            List<DBCluster> accountDbClusters = dbClusters.get(accountId);
            for (DBCluster cluster : accountDbClusters) {
                List<String> clusterTagKeys = cluster.tagList()
                        .stream()
                        .map(Tag::key)
                        .toList();
                for (String tagName : standardTagKeyNames) {
                    if (!clusterTagKeys.contains(tagName)) {
                        results.add(new StandardCheckResultResponseDTO(accountId, cluster.dbClusterIdentifier(), StandardCheckResultResponseDTO.StandardType.TAG, tagName, "태그생성필요", null, "표준 태그가 존재하지 않습니다."));
                    }
                }
            }
        }

        Map<String, List<DBInstance>> dbInstances = awsService.findAllInstanceInfo();
        for (String accountId : dbInstances.keySet()) {
            List<DBInstance> accountDbInstances = dbInstances.get(accountId);
            for (DBInstance dbInstance : accountDbInstances) {
                List<String> instanceTagKeys = dbInstance.tagList()
                        .stream()
                        .map(Tag::key)
                        .toList();
                for (String tagName : standardTagKeyNames) {
                    if (!instanceTagKeys.contains(tagName)) {
                        results.add(new StandardCheckResultResponseDTO(accountId, dbInstance.dbInstanceIdentifier(), StandardCheckResultResponseDTO.StandardType.TAG, tagName, "태그생성필요", null, "표준 태그가 존재하지 않습니다."));
                    }
                }
            }
        }
        return results;
    }

}
