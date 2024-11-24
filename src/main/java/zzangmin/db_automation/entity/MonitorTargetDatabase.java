package zzangmin.db_automation.entity;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MonitorTargetDatabase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountId;
    private String environment;
    private String serviceName;
    @Enumerated(EnumType.STRING)
    private DatabaseType databaseType;
    private String databaseName;
    private String databaseDriver;
    private String writerEndpoint;
    private String readerEndpoint;
    private int port;
    private String userName;
    private String password;
    private boolean isMonitorTarget;

    public enum DatabaseType {
        CLUSTER,
        INSTANCE,
        SERVERLESS,
        ON_PREMISE
    }

}
