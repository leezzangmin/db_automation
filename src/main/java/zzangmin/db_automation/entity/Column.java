package zzangmin.db_automation.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Objects;
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
    @JsonProperty("isNull")
    private boolean isNull; // NOT NULL, DEFAULT NULL
    private String defaultValue;
    @NotBlank
    @JsonProperty("isUnique")
    private boolean isUnique;
    @NotBlank
    @JsonProperty("isAutoIncrement")
    private boolean isAutoIncrement;
    @NotBlank
    private String comment;
    @NotBlank
    private String charset;
    @NotBlank
    private String collate;


    public String generateNull() {
        if (this.isNull) {
            if (Objects.isNull(this.defaultValue)) {
                return "DEFAULT NULL";
            }
            if (defaultValue.equals("null") || defaultValue.equals("NULL")) {
                return "DEFAULT NULL";
            }
            return "DEFAULT '" + this.defaultValue + "'";
        }
        return "NOT NULL";
    }

    public String generateUnique() {
        if (this.isUnique) {
            return " UNIQUE";
        }
        return "";
    }

    public String generateAutoIncrement() {
        if (this.isAutoIncrement) {
            return " AUTO_INCREMENT";
        }
        return "";
    }

    public int injectVarcharLength() {
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

    public void changeColumnType(String type) {
        this.type = type;
    }

    public String compare(Column other) {
        StringBuilder differences = new StringBuilder();

        if (!this.type.equals(other.type)) {
            differences.append(String.format("타입이 다릅니다: %s <-> %s. ", this.type, other.type));
        }
        if (this.isNull != other.isNull) {
            differences.append(String.format("NULL 가능 여부가 다릅니다: %s <-> %s. ", this.isNull, other.isNull));
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue)) { // Objects.equals는 null-safe 비교를 제공합니다.
            differences.append(String.format("기본값이 다릅니다: %s <-> %s. ", this.defaultValue, other.defaultValue));
        }
        if (this.isUnique != other.isUnique) {
            differences.append(String.format("고유 여부가 다릅니다: %s <-> %s. ", this.isUnique, other.isUnique));
        }
        if (this.isAutoIncrement != other.isAutoIncrement) {
            differences.append(String.format("자동 증가 여부가 다릅니다: %s <-> %s. ", this.isAutoIncrement, other.isAutoIncrement));
        }
        if (!this.comment.equals(other.comment)) {
            differences.append(String.format("설명이 다릅니다: %s <-> %s. ", this.comment, other.comment));
        }
        if (!this.charset.equals(other.charset)) {
            differences.append(String.format("문자셋이 다릅니다: %s <-> %s. ", this.charset, other.charset));
        }
        if (!this.collate.equals(other.collate)) {
            differences.append(String.format("콜레이션이 다릅니다: %s <-> %s. ", this.collate, other.collate));
        }

        return differences.toString();
    }

}
