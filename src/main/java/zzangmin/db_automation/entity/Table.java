package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import zzangmin.db_automation.util.ProfileUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Table {
    @NotBlank
    private String tableName;
    @NotBlank
    private Set<Column> columns = new HashSet<>();
    @NotBlank
    private Set<Constraint> constraints = new HashSet<>();
    @NotBlank
    private String tableEngine;
    @NotBlank
    private String tableCharset;
    @NotBlank
    private String tableCollate;
    @NotBlank
    private String tableComment;

    public void addColumns(List<Column> columns) {
        this.columns.addAll(columns);
    }

    public void addConstraints(List<Constraint> constraints) {
        this.constraints.addAll(constraints);
    }

    public String reportDifference(Table otherTable) {
        StringBuilder result = new StringBuilder();
        StringBuilder differences = new StringBuilder();

        if (otherTable == null) {
            result.append(String.format("`%s` 테이블을 %s에서 찾을 수 없습니다.\n", this.tableName, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
            return result.toString();
        }

        if (!this.tableName.equals(otherTable.tableName)) {
            differences.append(String.format("테이블 이름이 다릅니다: `%s` <-> `%s`\n", this.tableName, otherTable.tableName, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
        }
        if (!this.tableEngine.equals(otherTable.tableEngine)) {
            differences.append(String.format("테이블 엔진이 다릅니다: `%s` <-> `%s`\n", this.tableEngine, otherTable.tableEngine, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
        }
        if (!this.tableCharset.equals(otherTable.tableCharset)) {
            differences.append(String.format("테이블 문자셋이 다릅니다: `%s` <-> `%s`\n", this.tableCharset, otherTable.tableCharset, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
        }
        if (!this.tableCollate.equals(otherTable.tableCollate)) {
            differences.append(String.format("테이블 콜레이션 다릅니다: `%s` <-> `%s`\n", this.tableCollate, otherTable.tableCollate, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
        }
        if (!this.tableComment.equals(otherTable.tableComment)) {
            differences.append(String.format("테이블 코멘트가 다릅니다: `%s` <-> `%s`\n", this.tableComment, otherTable.tableComment));
        }
        if (this.columns.size() != (otherTable.columns.size())) {
            differences.append(String.format("컬럼 개수가 다릅니다: `%s` <-> `%s`\n", this.columns.size(), otherTable.columns.size()));
        }
        if (this.constraints.size() != (otherTable.constraints.size())) {
            differences.append(String.format("제약조건 개수가 다릅니다: `%s` <-> `%s`\n", this.constraints.size(), otherTable.constraints.size()));
        }

        for (Column thisColumn : this.columns) {
            boolean found = false;
            for (Column otherColumn : otherTable.columns) {
                if (thisColumn.getName().equals(otherColumn.getName())) {
                    found = true;
                    differences.append(thisColumn.reportDifference(otherColumn));
                    break;
                }
            }
            if (!found) {
                differences.append(String.format("컬럼 [`%s`]이/가 %s에 존재하지 않습니다.\n", thisColumn.getName(), ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
            }
        }

        for (Column otherColumn : otherTable.columns) {
            boolean found = false;
            for (Column thisColumn : this.columns) {
                if (otherColumn.getName().equals(thisColumn.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                differences.append(String.format("컬럼 [`%s`]이/가 prod에 존재하지 않습니다.\n", otherColumn.getName(), ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
            }
        }

        for (Constraint constraint : this.constraints) {
            boolean found = false;
            for (Constraint otherConstraint : otherTable.constraints) {
                if (constraint.getKeyName().equals(otherConstraint.getKeyName())) {
                    found = true;
                    String constraintDifferences = constraint.reportDifference(otherConstraint);
                    if (!constraintDifferences.isEmpty()) {
                        differences.append(String.format("\n제약조건 [`%s`] 차이점: %s\n", constraint.getKeyName(), constraintDifferences));
                    }
                    break;
                }
            }
            if (!found) {
                differences.append(String.format("제약조건 [`%s`]이/가 %s에 존재하지 않습니다.\n", constraint.getKeyName(), ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
            }
        }

        for (Constraint otherConstraint : otherTable.constraints) {
            boolean found = false;
            for (Constraint thisConstraint : this.constraints) {
                if (otherConstraint.getKeyName().equals(thisConstraint.getKeyName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                differences.append(String.format("제약조건 [`%s`]이/가 prod에 존재하지 않습니다.\n", otherConstraint.getKeyName(), ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
            }
        }

        if (differences.toString().isBlank()) {
            return result.toString();
        }

        result.append(String.format("\n테이블[`%s`] 검사 결과: \n", this.getTableName()));
        result.append(differences);
        result.append("\n");
        return result.toString();
    }
}
