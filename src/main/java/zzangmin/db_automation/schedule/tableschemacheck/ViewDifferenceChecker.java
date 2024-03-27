package zzangmin.db_automation.schedule.tableschemacheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.View;
import zzangmin.db_automation.service.SchemaObjectService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ViewDifferenceChecker {

    private final MysqlClient mysqlClient;
    private final SchemaObjectService schemaObjectService;

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

        log.info("ViewDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveViews(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) throws Exception {
        log.info("database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("serviceName: {}", serviceName);

        for (String schemaName : schemaNames) {
            List<View> views = mysqlClient.findViews(databaseConnectionInfo, schemaName);

            log.info("save views: {}", views);
            schemaObjectService.saveViews(serviceName, schemaName, views);
        }
    }

    public String compareViewCrossAccount(DatabaseConnectionInfo databaseConnectionInfo, List<String> schemaNames) {
        StringBuilder differenceResult = new StringBuilder();
        log.info("compareViewCrossAccount database: {}", databaseConnectionInfo);
        String serviceName = databaseConnectionInfo.findServiceName();
        log.info("compareViewCrossAccount serviceName: {}", serviceName);
        for (String schemaName : schemaNames) {
            log.info("schemaName: {}", schemaName);

            Map<String, View> prodViews = schemaObjectService.findViews(serviceName, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            view -> view.getViewName(),
                            view -> view));
            Map<String, View> currentViews = mysqlClient.findViews(databaseConnectionInfo, schemaName)
                    .stream()
                    .collect(Collectors.toMap(
                            view -> view.getViewName(),
                            view -> view));

            for (String prodViewName : prodViews.keySet()) {
                View prodView = prodViews.get(prodViewName);
                View currentView = currentViews.getOrDefault(prodViewName, null);
                differenceResult.append(prodView.reportDifference(currentView));
            }
        }

        log.info("compareViewCrossAccount Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

}
