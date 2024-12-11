package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParameterGroupStandard {

    private final static Map<String, String> standardParameters = new HashMap<>();

    static {
        standardParameters.put("max_connections", "10000");
        standardParameters.put("max_allowed_packet", "1073741824");
        standardParameters.put("character_set_connection", "utf8mb4");
        standardParameters.put("character_set_database", "utf8mb4");
        standardParameters.put("character_set_filesystem", "utf8mb4");
        standardParameters.put("character_set_server", "utf8mb4");
        standardParameters.put("character_set_results", "utf8mb4");
        standardParameters.put("collation_connection", "utf8mb4_0900_ai_ci");
        standardParameters.put("collation_server", "utf8mb4_0900_ai_ci");
        standardParameters.put("slow_query_log", "1");
        standardParameters.put("log_slow_extra", "ON");
        standardParameters.put("time_zone", "UTC");
        standardParameters.put("transaction_isolation", "REPEATABLE-READ");
        standardParameters.put("performance_schema", "1");
        standardParameters.put("log_bin_trust_function_creators", "1");
        standardParameters.put("innodb_print_ddl_logs", "1");
        standardParameters.put("lower_case_table_names", "1");
        standardParameters.put("innodb_monitor_enable", "all");
        standardParameters.put("activate_all_roles_on_login", "1");
        standardParameters.put("binlog_format", "ROW");
        standardParameters.put("binlog_row_image", "FULL");
        standardParameters.put("log_output", "FILE");
        standardParameters.put("sql_mode", "NO_ENGINE_SUBSTITUTION,ERROR_FOR_DIVISION_BY_ZERO,STRICT_TRANS_TABLES");
        standardParameters.put("general_log", "0");
        standardParameters.put("log_error_verbosity", "3");
        standardParameters.put("log_slow_admin_statements", "1");
        standardParameters.put("max_connect_errors", "1000000");
        standardParameters.put("sysdate-is-now", "1");
        standardParameters.put("interactive_timeout", "1800");
        standardParameters.put("wait_timeout", "1800");
        standardParameters.put("event_scheduler", "OFF");
        standardParameters.put("explicit_defaults_for_timestamp", "1");
        standardParameters.put("innodb_lock_wait_timeout", "15");
        standardParameters.put("innodb_print_all_deadlocks", "1");
        standardParameters.put("performance_schema_events_statements_history_size", "10000");
        standardParameters.put("performance_schema_events_statements_history_long_size", "10000");
        standardParameters.put("innodb_log_file_size", "3000000000"); // 3 GB
        standardParameters.put("innodb_flush_log_at_trx_commit", "1");
        standardParameters.put("innodb_buffer_pool_size", "{DBInstanceClassMemory*3/4}");
        standardParameters.put("sync_binlog", "1");
    }

    public static String findStandardValue(String parameterName) {
        return standardParameters.get(parameterName);
    }

    public static Set<String> getKeySet() {
        return standardParameters.keySet();
    }

    public static int getSize() {
        return standardParameters.size();
    }

}
