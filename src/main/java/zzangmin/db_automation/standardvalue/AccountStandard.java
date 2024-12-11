package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountStandard {
    private final static Map<String, Boolean> accountStandards = new HashMap<>();

    public final static String masterAccountEnableKey = "masterUserEnable";
    public final static String accountHostPercentAllowKey = "accountHostPercentAllow";

    // app 계정 허용 권한 목록
    public final static List<String> applicationAccountAllowedPrivileges = List.of("USAGE",
            "SELECT",
            "INSERT",
            "UPDATE",
            "DELETE",
            "EXECUTE");
    // 검사 비대상 계정명 목록
    public final static List<String> accountBlackList = List.of("rdsadmin",
            "rds_superuser_role",
            "root",
            "admin",
            "mysql.infoschema",
            "mysql.sys",
            "mysql.session",
            "db_monitor_august",
            "event_scheduler");

    static {
        accountStandards.put(masterAccountEnableKey, false);
        accountStandards.put(accountHostPercentAllowKey, false);
    }

    public static List<String> getApplicationAccountAllowedPrivileges() {
        return applicationAccountAllowedPrivileges;
    }

    public static List<String> getAccountBlackList() {
        return accountBlackList;
    }

    public static boolean isMasterUserEnable() {
        return accountStandards.get(masterAccountEnableKey);
    }

    public static boolean isAccountHostPercentAllow() {
        return accountStandards.get(accountHostPercentAllowKey);
    }

}
