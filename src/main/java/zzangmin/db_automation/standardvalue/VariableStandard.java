package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.Map;

public class VariableStandard {

    // variable 변수 (ex. show global variables like ... )
    private final static Map<String, String> variableStandards = new HashMap<>();

    // performance schema 의 테이블 설정 값 등
    private final static Map<String, String> valueStandard = new HashMap<>();

    static {
        variableStandards.put("binlog_expire_logs_seconds", "604800"); // --> 168시간, 1주일
    }

    static {
        valueStandard.put("SELECT enabled FROM performance_schema.setup_consumers WHERE name LIKE 'stage/innodb/alter%'", "YES");
        valueStandard.put("SELECT enabled FROM performance_schema.setup_instruments WHERE name LIKE '%events_stages_%", "YES");
    }

}
