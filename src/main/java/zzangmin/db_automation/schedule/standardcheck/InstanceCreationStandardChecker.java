package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.DBInstance;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.InstanceCreationStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class InstanceCreationStandardChecker {

    private final AwsService awsService;

    public String checkInstanceCreationStandard() {
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
}
