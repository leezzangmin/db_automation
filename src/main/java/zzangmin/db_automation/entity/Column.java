package zzangmin.db_automation.entity;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Column {
    private String name;
    private String type; // varchar(123), bigint, datetime
    private boolean isNull; // NOT NULL, DEFAULT NULL
    private String defaultValue;
    private boolean isUnique;
    private boolean isAutoIncrement;
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
