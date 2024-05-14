package zzangmin.db_automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.rds.model.Tag;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;

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

    public String findServiceName() {
        if (tags.isEmpty()) {
            throw new IllegalStateException(this.databaseName + "DB tag 가 비어있습니다.");
        }
        for (Tag tag : tags) {
            if (tag.key().equals(TagStandard.getServiceTagKeyName())) {
                return tag.value();
            }
        }
        throw new IllegalStateException(TagStandard.getStandardTagKeyNames() + "태그가 없습니다. DB명: " + databaseName);
    }
}
