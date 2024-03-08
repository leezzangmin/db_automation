package zzangmin.db_automation.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

@ToString
@Getter
@Builder
@AllArgsConstructor
public class DatabaseConnectionInfo {

    private String databaseName;
    private String driverClassName;
    private String url;
    private String username;
    private String password;


    public DatabaseConnectionInfo getApplicationDatabaseConnectionInfo(
            @Value("${spring.datasource.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.usernam}") String username,
            @Value("${spring.datasource.password}") String password) {
        this.databaseName = null;
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String databaseSummary() {
        return this.databaseName + " (" + this.url + ")\n";
    }


}
