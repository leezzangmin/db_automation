package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterParametersResponse;
import software.amazon.awssdk.services.rds.model.Parameter;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.ParameterGroupStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ParameterStandardChecker {

    private final AwsService awsService;

    public String checkParameterStandard() {
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
}
