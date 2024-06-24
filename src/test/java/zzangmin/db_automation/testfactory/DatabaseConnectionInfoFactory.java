package zzangmin.db_automation.testfactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rds.model.Tag;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;

import java.util.ArrayList;
import java.util.List;


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
                .environment("stage")
                .accountId("123123")
                .serviceName("order")
                .databaseType(DatabaseConnectionInfo.DatabaseType.CLUSTER)
                .databaseName("inhouse")
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}

