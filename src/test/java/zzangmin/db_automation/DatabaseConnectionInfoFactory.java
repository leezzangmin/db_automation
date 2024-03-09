package zzangmin.db_automation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;


@Component
public class DatabaseConnectionInfoFactory {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    private String databaseName;

    public DatabaseConnectionInfo createDatabaseConnectionInfo() {
        return DatabaseConnectionInfo.builder()
                .databaseName("inhouse")
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}

