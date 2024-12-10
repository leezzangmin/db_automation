package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.Map;

public class VariableStandard {

    private final static Map<String, String> variableStandard = new HashMap<>();

    static {
        variableStandard.put("binlog_expire_logs_seconds", "604800"); // --> 168시간, 1주일
    }

}
