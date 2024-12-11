package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VariableStandard {

    // variable 변수 (ex. show global variables like ... )
    private final static Map<String, String> variableStandards = new HashMap<>();

    // performance schema 의 테이블 설정 값 등
    private final static Map<String, String> valueStandards = new HashMap<>();

    static {
        variableStandards.put("binlog_expire_logs_seconds", "604800"); // --> 168시간, 1주일
    }

    static {
        valueStandards.put("SELECT enabled FROM performance_schema.setup_consumers WHERE name LIKE 'stage/innodb/alter%'", "YES");
        valueStandards.put("SELECT enabled FROM performance_schema.setup_instruments WHERE name LIKE '%events_stages_%", "YES");
    }

    public static String findVariableStandardValue(String variableName) {
        return variableStandards.get(variableName);
    }

    public static String findValueStandardValue(String valueNmae) {
        return valueStandards.get(valueNmae);
    }

    public static Set<String> getVariableKeySet() {
        return variableStandards.keySet();
    }

    public static Set<String> getValueKeySet() {
        return valueStandards.keySet();
    }
}
