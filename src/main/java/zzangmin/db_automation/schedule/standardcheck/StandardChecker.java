package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.*;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class StandardChecker {

    private final AwsService awsService;
    private final SlackClient slackClient;

    @Scheduled(fixedDelay = 1000000)
    public void checkStandard() {
        String parameterCheckResult = checkParameterStandard();
        String clusterCreationStandardResult = checkClusterCreationStandard();
        String instanceCreationStandardResult = checkInstanceCreationStandard();

        StringBuilder sb = new StringBuilder();
        sb.append(parameterCheckResult);
        sb.append(clusterCreationStandardResult);
        sb.append(instanceCreationStandardResult);

        slackClient.sendMessage(sb.toString());
    }

    private String checkParameterStandard() {
        StringBuilder sb = new StringBuilder();
        List<String> clusterParameterGroupNames = awsService.findParameterGroupNames();

        for (String clusterParameterGroupName : clusterParameterGroupNames) {
            DescribeDbClusterParametersResponse clusterParameterGroup = awsService.findClusterParameterGroup(clusterParameterGroupName);

            List<Parameter> parameters = clusterParameterGroup.parameters();
            for (Parameter parameter : parameters) {
                if (!ParameterGroupStandard.findStandardValue(parameter.parameterName()).equals(parameter.parameterValue())) {
                    sb.append(String.format("\nCluster Parameter Group Name: %s, 비표준 파라미터명: %s, 표준값: %s, 현재값: %s", clusterParameterGroupName, parameter.parameterName(), ParameterGroupStandard.findStandardValue(parameter.parameterName()), parameter.parameterValue()));
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String checkClusterCreationStandard() {
        StringBuilder sb = new StringBuilder();
        DescribeDbClustersResponse response = awsService.findAllClusterInfo();
        List<DBCluster> dbClusters = response.dbClusters();
        for (DBCluster dbCluster : dbClusters) {
            Set<String> creationStandardNames = ClusterCreationStandard.clusterCreationStandard.keySet();
            for (String creationStandardName : creationStandardNames) {
                String value = String.valueOf(dbCluster.getValueForField(creationStandardName, Object.class)
                        .orElseThrow(() -> new IllegalArgumentException("해당 필드가 존재하지 않습니다." + creationStandardName)));
                if (!ClusterCreationStandard.findStandardValue(creationStandardName).equals(value)) {
                    sb.append(String.format("\nCluster Name: %s, 비표준 설정명: %s, 표준값: %s, 현재값: %s", dbCluster.dbClusterIdentifier(), creationStandardName, ClusterCreationStandard.findStandardValue(creationStandardName), value));
                }
            }
        }
        return sb.toString();
    }

    private String checkInstanceCreationStandard() {
        StringBuilder sb = new StringBuilder();
        List<DBInstance> dbInstances = awsService.findAllInstanceInfo();
        for (DBInstance dbInstance : dbInstances) {
            Set<String> instanceStandardNames = InstanceCreationStandard.instanceCreationStandard.keySet();
            for (String instanceStandardName : instanceStandardNames) {
                String value = String.valueOf(dbInstance.getValueForField(instanceStandardName, Object.class)
                        .orElseThrow(() -> new IllegalArgumentException("해당 필드가 존재하지 않습니다." + instanceStandardName)));
                if (!InstanceCreationStandard.findStandardValue(instanceStandardName).equals(value)) {
                    sb.append(String.format("\nInstance Name: %s, 비표준 설정명: %s, 표준값: %s, 현재값: %s", dbInstance.dbInstanceIdentifier(), instanceStandardName, InstanceCreationStandard.findStandardValue(instanceStandardName), value));
                }
            }
        }

        return sb.toString();
    }


    // 스키마, 계정 권한 등
    private String checkSchemaStandard() {
        return null;
    }
}
