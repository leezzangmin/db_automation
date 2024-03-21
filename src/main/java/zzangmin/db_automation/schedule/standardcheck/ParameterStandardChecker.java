package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterParametersResponse;
import software.amazon.awssdk.services.rds.model.Parameter;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.ParameterGroupStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ParameterStandardChecker {

    private final AwsService awsService;

    public String checkParameterStandard() {
        StringBuilder sb = new StringBuilder();
        List<String> dbParameterGroupNames = awsService.findDbParameterGroupNames();
        List<String> clusterParameterGroupNames = awsService.findClusterParameterGroupNames();
        log.info("dbParameterGroupNames: {}", dbParameterGroupNames);
        log.info("clusterParameterGroupNames: {}", clusterParameterGroupNames);

        for (String parameterGroupName : parameterGroupNames) {
            DescribeDbClusterParametersResponse parameterGroup = awsService.findParameterGroup(parameterGroupName);

            List<Parameter> parameters = parameterGroup.parameters();
            for (Parameter parameter : parameters) {
                if (!ParameterGroupStandard.findStandardValue(parameter.parameterName()).equals(parameter.parameterValue())) {
                    sb.append(String.format("\nParameter Group Name: %s, 비표준 파라미터명: %s, 표준값: %s, 현재값: %s", parameterGroupName, parameter.parameterName(), ParameterGroupStandard.findStandardValue(parameter.parameterName()), parameter.parameterValue()));
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
