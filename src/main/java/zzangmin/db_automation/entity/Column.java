package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Column {
    @NotBlank
    private String name;
    @NotBlank
    private String type; // varchar(123), bigint, datetime
    @NotBlank
    private boolean isNull; // NOT NULL, DEFAULT NULL
    private String defaultValue;
    @NotBlank
    private boolean isUnique;
    @NotBlank
    private boolean isAutoIncrement;
    @NotBlank
    private String comment;

    public String generateNull() {
        if (isNull) {
            return "DEFAULT NULL";
        }
        return "NOT NULL";
    }

    public String generateUnique() {
        if (isUnique) {
            return "UNIQUE";
        }
        return "";
    }

    public String generateAutoIncrement() {
        if (isAutoIncrement) {
            return "AUTO_INCREMENT";
        }
        return "";
    }
}
