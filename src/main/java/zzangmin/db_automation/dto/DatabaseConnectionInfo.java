package zzangmin.db_automation.dto;

import lombok.*;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;

import java.util.ArrayList;
import java.util.List;

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
    private String url;
    private String username;
    private String password;

    public enum DatabaseType {
        CLUSTER,
        INSTANCE,
        SERVERLESS,
        ON_PREMISE
    }

    public String databaseSummary() {
        return this.databaseName + " (" + this.url + ")\n";
    }

    public String findServiceName() {
        // TODO
        return "todo";
//        if (tags.isEmpty()) {
//            throw new IllegalStateException(this.databaseName + "DB tag 가 비어있습니다.");
//        }
//        for (Tag tag : tags) {
//            if (tag.getKey().equals(TagStandard.getServiceTagKeyName())) {
//                return tag.getValue();
//            }
//        }
//        throw new IllegalStateException(TagStandard.getStandardTagKeyNames() + "태그가 없습니다. DB명: " + databaseName);
    }
}
