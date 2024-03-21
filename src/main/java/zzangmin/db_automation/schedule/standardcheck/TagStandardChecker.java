package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbClustersResponse;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
public class TagStandardChecker {

    private final AwsService awsService;

    public String checkTagStandard() {
        StringBuilder tagStandardResult = new StringBuilder();

        List<String> standardTagKeyNames = TagStandard.standardTagKeyNames;
        List<DBCluster> dbClusters = awsService.findAllClusterInfo();
        List<DBInstance> instancesResponse = awsService.findAllInstanceInfo();

        for (DBCluster cluster : dbClusters) {
            List<String> clusterTagKeys = cluster.tagList()
                    .stream()
                    .map(tag -> tag.key())
                    .collect(Collectors.toList());
            for (String tagName : standardTagKeyNames) {
                if (!clusterTagKeys.contains(tagName)) {
                    tagStandardResult.append(String.format("%s 클러스터에 %s 태그가 존재하지 않습니다.\n", cluster.dbClusterIdentifier(), tagName));
                }
            }
        }
        for (DBInstance dbInstance : instancesResponse) {
            List<String> instanceTagKeys = dbInstance.tagList()
                    .stream()
                    .map(tag -> tag.key())
                    .collect(Collectors.toList());
            for (String tagName : standardTagKeyNames) {
                if (!instanceTagKeys.contains(tagName)) {
                    tagStandardResult.append(String.format("%s 인스턴스에 %s 태그가 존재하지 않습니다.\n", dbInstance.dbInstanceIdentifier(), tagName));
                }
            }
        }
        return tagStandardResult.toString();
    }

}
