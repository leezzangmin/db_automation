package zzangmin.db_automation.convention;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CommonConvention {

    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z]+(_[a-z]+)*$");
    private static final Pattern PURE_LOWER_CASE_PATTER = Pattern.compile("^[a-z_]+$");

    public void validateSnakeCase(String str) {
        if (!SNAKE_CASE_PATTERN.matcher(str).matches()) {
            throw new IllegalArgumentException("snake_case 가 아닙니다: " + str);
        }
    }

    public boolean validateLowerCaseString(String str) {
        return PURE_LOWER_CASE_PATTER.matcher(str).matches();
    }

}
