package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.Map;

public class ClusterCreationStandard {

    public final static Map<String, String> clusterCreationStandard = new HashMap<>();

    static {
        clusterCreationStandard.put("BackupRetentionPeriod", "7");
        clusterCreationStandard.put("MultiAZ", "true");
        clusterCreationStandard.put("DeletionProtection", "true");
        clusterCreationStandard.put("Engine", "aurora-mysql");
        clusterCreationStandard.put("EngineVersion", "8.0.mysql_aurora.3.03.1");
        clusterCreationStandard.put("Port", "3306");
        clusterCreationStandard.put("MasterUsername", "admin");
    }

    public static String findStandardValue(String parameterName) {
        return clusterCreationStandard.get(parameterName);
    }

}
