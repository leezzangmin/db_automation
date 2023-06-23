package zzangmin.db_automation.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
@AllArgsConstructor
public class DatabaseConnectionInfo {

    private String databaseName;
    private String driverClassName;
    private String url;
    private String username;
    // TODO: aws parameter store 에서 fetch
    private String password;
}
