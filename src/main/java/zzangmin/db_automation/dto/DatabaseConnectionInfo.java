package zzangmin.db_automation.dto;

import lombok.*;

@ToString(exclude = "password")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConnectionInfo {

    private String environment; // ex) dev, stage, prod, alpha, beta, local, on-prem, test
    private String accountId; // (AWS) account ID
    private String serviceName; // ex) order, cart, event, etc..
    private DatabaseType databaseType; // cluster, instance, serverless, on-premise
    private String databaseName; // db identifier
    private String driverClassName;
    private String writerEndpoint;
    private String readerEndpoint;
    private String username;
    private String password;

    public enum DatabaseType {
        CLUSTER,
        INSTANCE,
        SERVERLESS,
        ON_PREMISE
    }

    public String databaseSummary() {
        return this.databaseName + " (" + this.writerEndpoint + ")\n";
    }

}
