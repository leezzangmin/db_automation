package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
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


    // TODO: isNull, isUnique json 파싱 제대로 안됨
    public String generateNull() {
        System.out.println("this.isNull = " + this.isNull);
        if (this.isNull) {
            return "DEFAULT NULL";
        }
        return "NOT NULL";
    }

    public String generateUnique() {
        if (this.isUnique) {
            return "UNIQUE";
        }
        return "";
    }

    public String generateAutoIncrement() {
        if (this.isAutoIncrement) {
            return "AUTO_INCREMENT";
        }
        return "";
    }

    public int getVarcharLength() {
        if (this.type.matches("(?i)varchar\\(\\d+\\)")) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(this.type);

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
