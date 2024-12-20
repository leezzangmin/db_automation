package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InstanceCreationStandard {

    private final static Map<String, String> instanceCreationStandards = new HashMap<>();

    static {
        instanceCreationStandards.put("AutoMinorVersionUpgrade", "false");
        instanceCreationStandards.put("DeletionProtection", "true");
        instanceCreationStandards.put("PerformanceInsightsEnabled", "true");
        instanceCreationStandards.put("EnabledCloudwatchLogsExports", "[slowquery]");
        instanceCreationStandards.put("PubliclyAccessible", "false");
    }

    public static String findStandardValue(String parameterName) {
        return instanceCreationStandards.get(parameterName);
    }

    public static Set<String> getKeySet() {
        return instanceCreationStandards.keySet();
    }

}
