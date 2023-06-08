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
    private String comment;
}
