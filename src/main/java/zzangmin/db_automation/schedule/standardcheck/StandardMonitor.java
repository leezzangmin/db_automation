package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.SlackClient;

@RequiredArgsConstructor
@Component
public class StandardMonitor {

    private final ClusterCreationStandardChecker clusterCreationStandardChecker;
    private final InstanceCreationStandardChecker instanceCreationStandardChecker;
    private final ParameterStandardChecker parameterStandardChecker;
    private final SchemaStandardChecker schemaStandardChecker;
    private final TagStandardChecker tagStandardChecker;
    private final SlackClient slackClient;

    @Scheduled(fixedDelay = 1000000)
    public void checkStandard() {
        StringBuilder standardCheckResult = new StringBuilder();

        String parameterCheckResult = parameterStandardChecker.checkParameterStandard();
        System.out.println("parameterCheckResult = " + parameterCheckResult);
//        String clusterCreationStandardResult = clusterCreationStandardChecker.checkClusterCreationStandard();
//        String instanceCreationStandardResult = instanceCreationStandardChecker.checkInstanceCreationStandard();
//        String schemaStandardResult = schemaStandardChecker.checkSchemaStandard();
//        String tagStandardResult = tagStandardChecker.checkTagStandard();
//
//        standardCheckResult.append(parameterCheckResult);
//        standardCheckResult.append(clusterCreationStandardResult);
//        standardCheckResult.append(instanceCreationStandardResult);
//        standardCheckResult.append(schemaStandardResult);
//        standardCheckResult.append(tagStandardResult);

        //        slackClient.sendMessage(standardCheckResult.toString());
    }
}
