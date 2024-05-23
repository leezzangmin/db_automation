package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.SlackService;

@Slf4j
@RequiredArgsConstructor
@Component
public class StandardMonitor {

    private static final long STANDARD_CHECK_DELAY = 9999999999l;

    private final ClusterCreationStandardChecker clusterCreationStandardChecker;
    private final InstanceCreationStandardChecker instanceCreationStandardChecker;
    private final ParameterStandardChecker parameterStandardChecker;
    private final SchemaStandardChecker schemaStandardChecker;
    private final TagStandardChecker tagStandardChecker;
    private final AccountStandardChecker accountStandardChecker;
    private final SlackService slackService;

    //@Scheduled(fixedDelay = STANDARD_CHECK_DELAY)
    public void checkStandard() {
        StringBuilder standardCheckResult = new StringBuilder();
        log.info("standard check start");
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
        log.info("standard check finish");
        slackService.sendNormalStringMessage(standardCheckResult.toString());
    }
}
