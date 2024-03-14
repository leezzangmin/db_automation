package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.View;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ViewDifferenceChecker {

    private final MysqlClient mysqlClient;

    public String compareView(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();

        for (String schemaName : schemaNames) {
            Map<String, View> sourceViews = mysqlClient.findViews(sourceInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            view -> view.getViewName(),
                            view -> view));
            Map<String, View> replicaViews = mysqlClient.findViews(replicaInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            view -> view.getViewName(),
                            view -> view));

            for (String sourceViewName : sourceViews.keySet()) {
                View sourceView = sourceViews.get(sourceViewName);
                View replicaView = replicaViews.getOrDefault(sourceViewName, null);
                differenceResult.append(sourceView.reportDifference(replicaView));
            }
        }
        return differenceResult.toString();
    }

}
