package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.Trigger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TriggerDifferenceChecker {

    private final MysqlClient mysqlClient;

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

        return differenceResult.toString();
    }
}
