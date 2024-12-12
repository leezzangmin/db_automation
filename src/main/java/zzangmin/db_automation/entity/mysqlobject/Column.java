package zzangmin.db_automation.entity.mysqlobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.List;
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
    private Boolean isNull; // NOT NULL, DEFAULT NULL
    private String defaultValue;
    @NotBlank
    @JsonProperty("isAutoIncrement")
    private Boolean isAutoIncrement;
    @NotBlank
    private String comment;
    private String charset;
    private String collate;


    public String generateNull() {
        if (this.isNull) {
            if (Objects.isNull(this.defaultValue)) {
                return "DEFAULT NULL";
            }
            if (defaultValue.equals("null") || defaultValue.equals("NULL")) {
                return "DEFAULT NULL";
            }
            if (defaultValue.equals("CURRENT_TIMESTAMP") || defaultValue.equals("current_timestamp")) {
                return "DEFAULT CURRENT_TIMESTAMP";
            }
            return "DEFAULT '" + this.defaultValue + "'";
        } else if (!this.isNull) {
            if (defaultValue == null || defaultValue.equals("null") || defaultValue.equals("NULL")) {
                return "NOT NULL";
            }
            if (defaultValue.equals("CURRENT_TIMESTAMP") || defaultValue.equals("current_timestamp")) {
                return "NOT NULL DEFAULT CURRENT_TIMESTAMP";
            }
            return "NOT NULL DEFAULT '" + this.defaultValue + "'";
        }
        return "NOT NULL";
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

    public String reportDifference(Column other) {
        StringBuilder differenceResult = new StringBuilder();
        StringBuilder differences = new StringBuilder();
        if (!Objects.equals(this.type, other.type)) {
            differences.append(String.format("타입이 다릅니다: `%s` <-> `%s`\n", this.type, other.type));
        }
        if (!Objects.equals(this.isNull, other.isNull)) {
            differences.append(String.format("NULL 가능 여부가 다릅니다: `%s` <-> `%s`\n", this.isNull, other.isNull));
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue)) { // Objects.equals는 null-safe
            differences.append(String.format("기본값이 다릅니다: `%s` <-> `%s`\n", this.defaultValue, other.defaultValue));
        }
        if (!Objects.equals(this.isAutoIncrement, other.isAutoIncrement)) {
            differences.append(String.format("자동 증가 여부가 다릅니다: `%s` <-> `%s`\n", this.isAutoIncrement, other.isAutoIncrement));
        }
        if (!Objects.equals(this.comment, other.comment)) {
            differences.append(String.format("코멘트가 다릅니다: `%s` <-> `%s`\n", this.comment, other.comment));
        }
        if (!Objects.equals(this.collate, other.collate)) {
            differences.append(String.format("콜레이션이 다릅니다: `%s` <-> `%s`\n", this.collate, other.collate));
        }

        if (!differences.isEmpty()) {
            differenceResult.append(String.format("\n컬럼 [`%s`] 차이점:\n", this.name));
            differenceResult.append(differences);
        }

        return differenceResult.toString();
    }

    public static Column of(ColumnDefinition columnDefinition) {
        List<String> columnSpecs = columnDefinition.getColumnSpecs();
        int collateColumnSpecIndex = -1;
        if (columnSpecs.indexOf("collate") != -1) {
            collateColumnSpecIndex = columnSpecs.indexOf("collate");
        } else if (columnSpecs.indexOf("COLLATE") != -1) {
            collateColumnSpecIndex = columnSpecs.indexOf("COLLATE");
        }
        int defaultColumnSpecIndex = -1;
        if (columnSpecs.indexOf("default") != -1) {
            defaultColumnSpecIndex = columnSpecs.indexOf("default");
        } else if (columnSpecs.indexOf("DEFAULT") != -1) {
            defaultColumnSpecIndex = columnSpecs.indexOf("DEFAULT");
        }

        Column column = Column.builder()
                .name(columnDefinition.getColumnName())
                .type(columnDefinition.getColDataType().toString().replace(" ", ""))
                .isNull((columnDefinition.getColumnSpecs().contains("NOT") ||
                        columnDefinition.getColumnSpecs().contains("not") ||
                        columnDefinition.getColumnSpecs().contains("primary") ||
                        columnDefinition.getColumnSpecs().contains("PRIMARY")) ? false : true)
                .defaultValue(defaultColumnSpecIndex == -1 ? null : columnSpecs.get(defaultColumnSpecIndex + 1))
                .isAutoIncrement(columnDefinition.getColumnSpecs().contains("auto_increment") || columnDefinition.getColumnSpecs().contains("AUTO_INCREMENT"))
                .collate(collateColumnSpecIndex == -1 ? null : columnSpecs.get(collateColumnSpecIndex + 1))
                .comment((columnDefinition.getColumnSpecs().contains("comment") || columnDefinition.getColumnSpecs().contains("COMMENT")) ?
                        columnDefinition.getColumnSpecs().get(columnDefinition.getColumnSpecs().size() - 1).replace("'","") : null)
                .build();
        return column;
    }

}
