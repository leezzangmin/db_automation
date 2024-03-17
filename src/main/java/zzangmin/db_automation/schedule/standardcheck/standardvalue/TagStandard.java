package zzangmin.db_automation.schedule.standardcheck.standardvalue;

import java.util.ArrayList;
import java.util.List;

public class TagStandard {

    public final static List<String> standardTagKeyNames = new ArrayList<>();

    private final static String ENVIRONMENT_TAG_KEY_NAME = "env";
    private final static String SERVICE_TAG_KEY_NAME = "service";

    static {
        standardTagKeyNames.add(ENVIRONMENT_TAG_KEY_NAME);
        standardTagKeyNames.add(SERVICE_TAG_KEY_NAME);
    }

    public static String getStandardTagKeyNames() {
        return standardTagKeyNames.toString();
    }

    public static String getEnvironmentTagKeyName() {
        return ENVIRONMENT_TAG_KEY_NAME;
    }

    public static String getServiceTagKeyName() {
        return SERVICE_TAG_KEY_NAME;
    }
}
