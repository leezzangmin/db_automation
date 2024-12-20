package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.mysqlobject.Trigger;
import zzangmin.db_automation.service.SchemaObjectService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TriggerDifferenceChecker {

    private final MysqlClient mysqlClient;
    private final SchemaObjectService schemaObjectService;

    public String compareTrigger(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();

        for (String schemaName : schemaNames) {

            Map<String, Trigger> sourceTriggers = mysqlClient.findTriggers(sourceInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            trigger -> trigger.getTriggerName(),
                            trigger -> trigger
                    ));

            Map<String, Trigger> replicaTriggers = mysqlClient.findTriggers(replicaInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            trigger -> trigger.getTriggerName(),
                            trigger -> trigger
                    ));

            for (String sourceTriggerName : sourceTriggers.keySet()) {
                Trigger sourceTrigger = sourceTriggers.get(sourceTriggerName);
                Trigger replicaTrigger = replicaTriggers.getOrDefault(sourceTriggerName, null);
                differenceResult.append(sourceTrigger.reportDifference(replicaTrigger));
            }

        }
        log.info("TriggerDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveTriggers(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.getServiceName();
        log.info("serviceName: {}", serviceName);

        for (String schemaName : schemaNames) {
            List<Trigger> triggers = mysqlClient.findTriggers(databaseConnectionInfo, schemaName);

            log.info("save triggers: {}", triggers);
            schemaObjectService.saveTriggers(serviceName, schemaName, triggers);
        }
    }

    public String compareTriggerCrossAccount(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        StringBuilder differenceResult = new StringBuilder();
        log.info("compareTriggerCrossAccount database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.getServiceName();
        log.info("compareTriggerCrossAccount serviceName: {}", serviceName);

        Map<String, Trigger> prodTriggers = schemaObjectService.findTriggers(serviceName, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        trigger -> trigger.getTriggerName(),
                        trigger -> trigger));
        Map<String, Trigger> currentTriggers = mysqlClient.findTriggers(databaseConnectionInfo, schemaName)
                .stream()
                .collect(Collectors.toMap(
                        trigger -> trigger.getTriggerName(),
                        trigger -> trigger));

        for (String prodTriggerName : prodTriggers.keySet()) {
            Trigger prodTrigger = prodTriggers.get(prodTriggerName);
            Trigger currentTrigger = currentTriggers.getOrDefault(prodTriggerName, null);
            differenceResult.append(prodTrigger.reportDifference(currentTrigger));
        }

        log.info("compareTriggerCrossAccount Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }
}
