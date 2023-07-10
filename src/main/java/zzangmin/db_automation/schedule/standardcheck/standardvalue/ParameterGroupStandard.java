package zzangmin.db_automation.schedule.standardcheck.standardvalue;

import java.util.HashMap;
import java.util.Map;

public class ParameterGroupStandard {

    public final static Map<String, String> standardParameters = new HashMap<>();

    static {
        standardParameters.put("max_connections", "10000");
        standardParameters.put("character_set_connection", "utf8mb4");
        standardParameters.put("character_set_database", "utf8mb4");
        standardParameters.put("character_set_filesystem", "utf8mb4");
        standardParameters.put("character_set_server", "utf8mb4");
        standardParameters.put("character_set_results", "utf8mb4");
        standardParameters.put("collation_connection", "utf8mb4_0900_ai_ci");
        standardParameters.put("collation_server", "utf8mb4_0900_ai_ci");
        standardParameters.put("slow_query_log", "1");
        standardParameters.put("time_zone", "UTC");
        standardParameters.put("transaction_isolation", "REPEATABLE-READ");
        standardParameters.put("performance_schema", "1");
    }

    public static String findStandardValue(String parameterName) {
        return standardParameters.get(parameterName);
    }

}
