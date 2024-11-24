package zzangmin.db_automation.entity.mysqlobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class Query {

    private String databaseName;
    private int processlistId;
    private String user;
    private String host;
    private String eventName;
    private String sqlText;
    private String digestText;
}
