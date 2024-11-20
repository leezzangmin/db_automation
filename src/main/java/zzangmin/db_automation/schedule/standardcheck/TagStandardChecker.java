package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DBInstance;
import zzangmin.db_automation.standardvalue.TagStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
public class TagStandardChecker {

    private final AwsService awsService;

    public String checkTagStandard() {
        StringBuilder tagStandardResult = new StringBuilder();
        List<String> standardTagKeyNames = TagStandard.standardTagKeyNames;
        Map<String, List<DBCluster>> dbClusters = awsService.findAllClusterInfo();
        for (String accountId : dbClusters.keySet()) {
            List<DBCluster> accountDbClusters = dbClusters.get(accountId);
            for (DBCluster cluster : accountDbClusters) {
                List<String> clusterTagKeys = cluster.tagList()
                        .stream()
                        .map(tag -> tag.key())
                        .collect(Collectors.toList());
                for (String tagName : standardTagKeyNames) {
                    if (!clusterTagKeys.contains(tagName)) {
                        tagStandardResult.append(String.format("accountId: %s, %s 클러스터에 %s 태그가 존재하지 않습니다.\n", accountId, cluster.dbClusterIdentifier(), tagName));
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
                        .map(tag -> tag.key())
                        .collect(Collectors.toList());
                for (String tagName : standardTagKeyNames) {
                    if (!instanceTagKeys.contains(tagName)) {
                        tagStandardResult.append(String.format("accountId: %s, %s 인스턴스에 %s 태그가 존재하지 않습니다.\n", accountId, dbInstance.dbInstanceIdentifier(), tagName));
                    }
                }
            }
        }

        return tagStandardResult.toString();
    }

}
