package zzangmin.db_automation.schedule.standardcheck.standardvalue;

import java.util.ArrayList;
import java.util.List;

public class TagStandard {

    public final static List<String> standardTagKeyNames = new ArrayList<>();

    static {
        standardTagKeyNames.add("env");
        standardTagKeyNames.add("service");
    }

}
