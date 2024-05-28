package zzangmin.db_automation.dto;

import lombok.*;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConnectionInfo {

    private String accountName;
    private String databaseName;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private List<Tag> tags = new ArrayList<>();


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tag {
        private String key;
        private String value;
    }


    public String databaseSummary() {
        return this.databaseName + " (" + this.url + ")\n";
    }

    public String findServiceName() {
        if (tags.isEmpty()) {
            throw new IllegalStateException(this.databaseName + "DB tag 가 비어있습니다.");
        }
        for (Tag tag : tags) {
            if (tag.getKey().equals(TagStandard.getServiceTagKeyName())) {
                return tag.getValue();
            }
        }
        throw new IllegalStateException(TagStandard.getStandardTagKeyNames() + "태그가 없습니다. DB명: " + databaseName);
    }
}
