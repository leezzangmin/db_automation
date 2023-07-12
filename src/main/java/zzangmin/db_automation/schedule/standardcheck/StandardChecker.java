package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.SlackClient;

@RequiredArgsConstructor
@Component
public class StandardChecker {

    private final ClusterCreationStandardChecker clusterCreationStandardChecker;
    private final InstanceCreationStandardChecker instanceCreationStandardChecker;
    private final ParameterStandardChecker parameterStandardChecker;
    private final SchemaStandardChecker schemaStandardChecker;
    private final SlackClient slackClient;

    //@Scheduled(fixedDelay = 1000000)
    public void checkStandard() {
        String parameterCheckResult = parameterStandardChecker.checkParameterStandard();
        String clusterCreationStandardResult = clusterCreationStandardChecker.checkClusterCreationStandard();
        String instanceCreationStandardResult = instanceCreationStandardChecker.checkInstanceCreationStandard();
        String schemaStandardResult = schemaStandardChecker.checkSchemaStandard();

        StringBuilder sb = new StringBuilder();
        sb.append(parameterCheckResult);
        sb.append(clusterCreationStandardResult);
        sb.append(instanceCreationStandardResult);
        sb.append(schemaStandardResult);

        slackClient.sendMessage(sb.toString());
    }
}
