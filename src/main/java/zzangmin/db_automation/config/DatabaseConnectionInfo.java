package zzangmin.db_automation.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class DatabaseConnectionInfo {

    private String databaseName;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
}