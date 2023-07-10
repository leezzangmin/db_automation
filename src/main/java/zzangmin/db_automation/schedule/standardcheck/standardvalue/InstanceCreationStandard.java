package zzangmin.db_automation.schedule.standardcheck.standardvalue;

import java.util.HashMap;
import java.util.Map;

public class InstanceCreationStandard {

    public final static Map<String, String> instanceCreationStandard = new HashMap<>();

    static {
        instanceCreationStandard.put("AutoMinorVersionUpgrade", "false");
        instanceCreationStandard.put("DeletionProtection", "true");
        instanceCreationStandard.put("PerformanceInsightsEnabled", "true");
        instanceCreationStandard.put("EnabledCloudwatchLogsExports", "[slowquery]");
        instanceCreationStandard.put("TagList", "[]");
    }

    public static String findStandardValue(String parameterName) {
        return instanceCreationStandard.get(parameterName);
    }

}
