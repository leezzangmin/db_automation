package zzangmin.db_automation.dto;

import lombok.*;
import zzangmin.db_automation.entity.MonitorTargetDatabase;

@ToString(exclude = "password")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConnectionInfo {

    private String environment; // ex) dev, stage, prod, alpha, beta, local, on-prem, test
    private String accountId; // (AWS) account ID
    private String serviceName; // ex) order, cart, event, etc..
    private MonitorTargetDatabase.DatabaseType databaseType; // cluster, instance, serverless, on-premise
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

    public String generateReadOnlyConnectionUrl() {
        if (this.readerEndpoint.startsWith("jdbc:mysql://")) {
            return this.readerEndpoint;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://");
        sb.append(this.readerEndpoint);
        sb.append(":");
        sb.append(this.port);
        sb.append("/");
        return sb.toString();
    }

    public String generateWriterConnectionUrl() {
        if (this.writerEndpoint.startsWith("jdbc:mysql://")) {
            return this.writerEndpoint;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://");
        sb.append(this.readerEndpoint);
        sb.append(":");
        sb.append(this.port);
        sb.append("/");
        return sb.toString();
    }

    public static DatabaseConnectionInfo of(MonitorTargetDatabase monitorTargetDatabase) {
        return new DatabaseConnectionInfo(monitorTargetDatabase.getEnvironment(),
                monitorTargetDatabase.getAccountId(),
                monitorTargetDatabase.getServiceName(),
                monitorTargetDatabase.getDatabaseType(),
                monitorTargetDatabase.getDatabaseName(),
                monitorTargetDatabase.getDatabaseDriver(),
                monitorTargetDatabase.getWriterEndpoint(),
                monitorTargetDatabase.getReaderEndpoint(),
                monitorTargetDatabase.getPort(),
                monitorTargetDatabase.getUsername(),
                monitorTargetDatabase.getPassword()
                );
    }

    public MonitorTargetDatabase toMonitorTargetDatabase() {
        return new MonitorTargetDatabase(null,
                this.accountId,
                this.environment,
                this.serviceName,
                this.databaseType,
                this.databaseName,
                this.driverClassName,
                this.writerEndpoint,
                this.readerEndpoint,
                this.port,
                this.username,
                this.password,
                true);
    }

}
