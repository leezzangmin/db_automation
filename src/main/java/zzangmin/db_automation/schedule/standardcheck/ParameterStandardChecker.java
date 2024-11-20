package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.Parameter;
import zzangmin.db_automation.standardvalue.ParameterGroupStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ParameterStandardChecker {

    private final AwsService awsService;

    public String checkParameterStandard() {
        StringBuilder sb = new StringBuilder();
        Map<String, Map<String, String>> accountIdClusterParameterGroupNames = awsService.findClusterParameterGroupNames();
        Map<String, Map<String, String>> accountIdInstanceParameterGroupNames = awsService.findDbParameterGroupNames();
        log.info("accountIdInstanceParameterGroupNames: {}", accountIdInstanceParameterGroupNames);
        log.info("accountIdClusterParameterGroupNames: {}", accountIdClusterParameterGroupNames);

        for (String accountId : accountIdClusterParameterGroupNames.keySet()) {
            Map<String, String> parameterGroupNames = accountIdClusterParameterGroupNames.get(accountId);
            for (String clusterIdentifier : parameterGroupNames.keySet()) {
                List<Parameter> clusterParameters = awsService.findClusterParameterGroupParameters(accountId, parameterGroupNames.get(clusterIdentifier));
                for (Parameter parameter : clusterParameters) {
                    if (!ParameterGroupStandard.findStandardValue(parameter.parameterName()).equals(parameter.parameterValue())) {
                        sb.append(String.format("\nCluster Parameter Group Name: %s, 비표준 파라미터명: %s, 표준값: %s, 현재값: %s",
                                parameterGroupNames.get(clusterIdentifier),
                                parameter.parameterName(),
                                ParameterGroupStandard.findStandardValue(parameter.parameterName()),
                                parameter.parameterValue())
                        );
                    }
                }
            }
        }

        for (String accountId : accountIdInstanceParameterGroupNames.keySet()) {
            Map<String, String> parameterGroupNames = accountIdInstanceParameterGroupNames.get(accountId);
            for (String instanceIdentifier : parameterGroupNames.keySet()) {
                List<Parameter> instanceParameters = awsService.findDbParameterGroupParameters(accountId, parameterGroupNames.get(instanceIdentifier));
                for (Parameter parameter : instanceParameters) {
                    if (!ParameterGroupStandard.findStandardValue(parameter.parameterName()).equals(parameter.parameterValue())) {
                        sb.append(String.format("\ninstance Parameter Group Name: %s, 비표준 파라미터명: %s, 표준값: %s, 현재값: %s",
                                parameterGroupNames.get(instanceIdentifier),
                                parameter.parameterName(),
                                ParameterGroupStandard.findStandardValue(parameter.parameterName()),
                                parameter.parameterValue())
                        );
                    }
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
