package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    @NotBlank
    private String name;
    @NotBlank
    private String type; // varchar(123), bigint, datetime
    @NotBlank
    private boolean isNull; // NOT NULL, DEFAULT NULL
    private String defaultValue;
    @NotBlank
    private boolean isUnique;
    @NotBlank
    private boolean isAutoIncrement;
    @NotBlank
    private String comment;
    @NotBlank
    private String charset;
    @NotBlank
    private String collate;

    public String generateNull() {
        if (isNull) {
            return "DEFAULT NULL";
        }
        return "NOT NULL";
    }

    public String generateUnique() {
        if (isUnique) {
            return "UNIQUE";
        }
        return "";
    }

    public String generateAutoIncrement() {
        if (isAutoIncrement) {
            return "AUTO_INCREMENT";
        }
        return "";
    }

    public int getVarcharLength() {
        if (this.type.matches("(?i)varchar\\(\\d+\\)")) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(type);

            if (matcher.find()) {
                String extractedNumber = matcher.group();
                return Integer.valueOf(extractedNumber);
            } else {
                throw new IllegalArgumentException(this.name + "컬럼 varchar 타입에 숫자가 표기되어있지 않습니다.");
            }
        }
        throw new IllegalStateException(this.name + " 컬럼은 varchar 타입이 아닙니다. 정상 입력 ex: VARCHAR(255)");
    }


}
