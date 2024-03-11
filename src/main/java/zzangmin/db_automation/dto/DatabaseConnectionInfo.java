package zzangmin.db_automation.dto;

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




    public String databaseSummary() {
        return this.databaseName + " (" + this.url + ")\n";
    }


}
