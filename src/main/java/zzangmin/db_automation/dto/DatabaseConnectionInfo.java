package zzangmin.db_automation.dto;

import lombok.*;
import zzangmin.db_automation.entity.MonitorTargetDb;

@ToString(exclude = "password")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConnectionInfo {

    private String environment; // ex) dev, stage, prod, alpha, beta, local, on-prem, test
    private String accountId; // (AWS) account ID
    private String serviceName; // ex) order, cart, event, etc..
    private MonitorTargetDb.DatabaseType databaseType; // cluster, instance, serverless, on-premise
    private String databaseName; // db identifier
    private String driverClassName;
    private String writerEndpoint;
    private String readerEndpoint;
    private int port;
    private String username;
    private String password;


    public String databaseSummary() {
        return this.databaseName + " (" + this.writerEndpoint + ")\n";
    }

    public static DatabaseConnectionInfo of(MonitorTargetDb monitorTargetDb) {
        return new DatabaseConnectionInfo(monitorTargetDb.getEnvironment(),
                monitorTargetDb.getAccountId(),
                monitorTargetDb.getServiceName(),
                monitorTargetDb.getDatabaseType(),
                monitorTargetDb.getDatabaseName(),
                monitorTargetDb.getDatabaseDriver(),
                monitorTargetDb.getWriterEndpoint(),
                monitorTargetDb.getReaderEndpoint(),
                monitorTargetDb.getPort(),
                monitorTargetDb.getUserName(),
                monitorTargetDb.getPassword()
                );
    }

}
