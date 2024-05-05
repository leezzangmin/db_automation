package zzangmin.db_automation.convention;


import zzangmin.db_automation.schedule.standardcheck.standardvalue.CommonStandard;

import java.util.Set;
import java.util.regex.Pattern;

public class CommonConvention {

    public final static int TABLE_BYTE_SIZE_THRESHOLD = 123456789;
    public final static int TABLE_ROW_SIZE_THRESHOLD = 9999999;
    public static final String ENGINE_TYPE = CommonStandard.ENGINE_TYPE;
    public static final String CHARSET = CommonStandard.CHARSET;
    public static final int BYTE_PER_CHARACTER = 4;
    public static final String COLLATE = CommonStandard.COLLATE;
    public static final Set<String> ALLOWED_CONSTRAINT_TYPE = Set.of("PRIMARY KEY", "UNIQUE KEY", "KEY");

    public static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z]+(_[a-z]+)*$");
    public static final Pattern PURE_LOWER_CASE_PATTER = Pattern.compile("^[a-z_]+$");

    public static void validateSnakeCase(String str) {
        if (!SNAKE_CASE_PATTERN.matcher(str).matches()) {
            throw new IllegalArgumentException("snake_case 가 아닙니다: " + str);
        }
    }

    public static boolean validateLowerCaseString(String str) {
        return PURE_LOWER_CASE_PATTER.matcher(str).matches();
    }

}
