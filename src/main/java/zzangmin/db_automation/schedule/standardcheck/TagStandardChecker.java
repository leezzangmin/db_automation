package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbClustersResponse;
import software.amazon.awssdk.services.rds.model.Tag;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TagStandardChecker {

    private final AwsService awsService;

    // prod, stage, dev 등의 현재 환경
    private final static String CURRENT_ENVIRONMENT = System.getenv("ENVIRONMENT");


    public String checkTagStandard() {
        StringBuilder tagStandardResult = new StringBuilder();

        List<String> standardTagKeyNames = TagStandard.standardTagKeyNames;
        DescribeDbClustersResponse clustersResponse = awsService.findAllClusterInfo();
        List<DBInstance> instancesResponse = awsService.findAllInstanceInfo();

        for (DBCluster cluster : clustersResponse.dbClusters()) {
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


    // 환경변수가 prod인데 tag의 값이 stage면 false 반환
    public static boolean isCurrentEnvHasValidTag(List<Tag> tags) {
        String tagEnv = "";
        for (Tag tag : tags) {
            if (tag.key().equals(TagStandard.getEnvironmentTagKeyName())) {
                tagEnv = tag.value();
                if (tag.value().equals(CURRENT_ENVIRONMENT)) {
                    return true;
                }
            }
        }
        log.info("현재 ENVIRONMENT에 맞지 않는 tag입니다. 현재 환경: {}, 입력된 태그의 환경: {}", CURRENT_ENVIRONMENT, tagEnv);
        return false;
    }
}
