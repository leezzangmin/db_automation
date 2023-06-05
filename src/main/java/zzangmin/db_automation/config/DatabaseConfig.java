package zzangmin.db_automation.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
public class DatabaseConfig {

    private String databaseName;
    private String driverClassName;
    private String url;
    private String username;
    private String password;

}