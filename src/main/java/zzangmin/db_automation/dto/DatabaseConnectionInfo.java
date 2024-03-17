package zzangmin.db_automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.rds.model.Tag;

import java.util.ArrayList;
import java.util.List;

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
    private List<Tag> tags = new ArrayList<>();




    public String databaseSummary() {
        return this.databaseName + " (" + this.url + ")\n";
    }


}
