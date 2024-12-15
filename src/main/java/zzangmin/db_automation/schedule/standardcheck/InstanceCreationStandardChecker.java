package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBInstance;
import zzangmin.db_automation.dto.response.check.StandardCheckResultResponseDTO;
import zzangmin.db_automation.standardvalue.InstanceCreationStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class InstanceCreationStandardChecker {

    private final AwsService awsService;

    public List<StandardCheckResultResponseDTO> checkInstanceCreationStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();
        Map<String, List<DBInstance>> dbInstances = awsService.findAllInstanceInfo();
        for (String accountId : dbInstances.keySet()) {
            List<DBInstance> accountDbInstances = dbInstances.get(accountId);
            for (DBInstance dbInstance : accountDbInstances) {
                Set<String> instanceStandardNames = InstanceCreationStandard.getKeySet();
                for (String instanceStandardName : instanceStandardNames) {
                    String findValue = String.valueOf(dbInstance.getValueForField(instanceStandardName, Object.class)
                            .orElseThrow(() -> new IllegalArgumentException("해당 필드가 존재하지 않습니다." + instanceStandardName)));
                    if (!InstanceCreationStandard.findStandardValue(instanceStandardName).equals(findValue)) {
                        results.add(new StandardCheckResultResponseDTO(accountId,
                                dbInstance.dbInstanceIdentifier(),
                                StandardCheckResultResponseDTO.StandardType.INSTANCE_CREATION,
                                instanceStandardName,
                                InstanceCreationStandard.findStandardValue(instanceStandardName),
                                findValue,
                                "인스턴스 생성 설정 값이 표준과 다릅니다."));
                    }
                }
            }
        }
        return results;
    }
}
