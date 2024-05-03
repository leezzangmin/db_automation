package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.SlackService;

@RequiredArgsConstructor
@Component
public class StandardMonitor {

    private final ClusterCreationStandardChecker clusterCreationStandardChecker;
    private final InstanceCreationStandardChecker instanceCreationStandardChecker;
    private final ParameterStandardChecker parameterStandardChecker;
    private final SchemaStandardChecker schemaStandardChecker;
    private final TagStandardChecker tagStandardChecker;
    private final AccountStandardChecker accountStandardChecker;
    private final SlackService slackService;

    //@Scheduled(fixedDelay = 1000000)
    public void checkStandard() {
        StringBuilder standardCheckResult = new StringBuilder();

        String parameterCheckResult = parameterStandardChecker.checkParameterStandard();
        String clusterCreationStandardResult = clusterCreationStandardChecker.checkClusterCreationStandard();
        String instanceCreationStandardResult = instanceCreationStandardChecker.checkInstanceCreationStandard();
        String schemaStandardResult = schemaStandardChecker.checkSchemaStandard();
        String tagStandardResult = tagStandardChecker.checkTagStandard();
        String accountStandardResult = accountStandardChecker.checkAccountStandard();

        standardCheckResult.append(parameterCheckResult);
        standardCheckResult.append(clusterCreationStandardResult);
        standardCheckResult.append(instanceCreationStandardResult);
        standardCheckResult.append(schemaStandardResult);
        standardCheckResult.append(tagStandardResult);
        standardCheckResult.append(accountStandardResult);

        slackService.sendMessage(standardCheckResult.toString());
    }
}
