package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.Parameter;
import zzangmin.db_automation.dto.response.check.StandardCheckResultResponseDTO;
import zzangmin.db_automation.standardvalue.ParameterGroupStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ParameterStandardChecker {

    private final AwsService awsService;

    public List<StandardCheckResultResponseDTO> checkParameterStandard() {

        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

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
                        results.add(new StandardCheckResultResponseDTO(accountId, clusterIdentifier, StandardCheckResultResponseDTO.StandardType.PARAMETER, parameter.parameterName(), ParameterGroupStandard.findStandardValue(parameter.parameterName()), parameter.parameterValue(), "클러스터 파라미터 설정 값이 표준과 다릅니다."));
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
                        results.add(new StandardCheckResultResponseDTO(accountId, instanceIdentifier, StandardCheckResultResponseDTO.StandardType.PARAMETER, parameter.parameterName(), ParameterGroupStandard.findStandardValue(parameter.parameterName()), parameter.parameterValue(), "인스턴스 파라미터 설정 값이 표준과 다릅니다."));
                    }
                }
            }
        }
        return results;
    }
}
