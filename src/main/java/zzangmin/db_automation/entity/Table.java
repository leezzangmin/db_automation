package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private List<Column> columns = new ArrayList<>();
    @NotBlank
    private List<Constraint> constraints = new ArrayList<>();
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
        StringBuilder differences = new StringBuilder();
        if (!this.tableName.equals(otherTable.tableName)) {
            differences.append(String.format("테이블 이름이 다릅니다: %s <-> %s%n", this.tableName, otherTable.tableName));
        }
        if (!this.tableEngine.equals(otherTable.tableEngine)) {
            differences.append(String.format("테이블 엔진이 다릅니다: %s <-> %s%n", this.tableEngine, otherTable.tableEngine));
        }
        if (!this.tableCharset.equals(otherTable.tableCharset)) {
            differences.append(String.format("테이블 문자셋이 다릅니다: %s <-> %s%n", this.tableCharset, otherTable.tableCharset));
        }
        if (!this.tableCollate.equals(otherTable.tableCollate)) {
            differences.append(String.format("테이블 콜레이션 다릅니다: %s <-> %s%n", this.tableCollate, otherTable.tableCollate));
        }
        if (!this.tableComment.equals(otherTable.tableComment)) {
            differences.append(String.format("테이블 설명이 다릅니다: %s <-> %s%n", this.tableComment, otherTable.tableComment));
        }
        if (this.columns.size() != (otherTable.columns.size())) {
            differences.append(String.format("컬럼 개수가 다릅니다: %s <-> %s%n", this.columns.size(), otherTable.columns.size()));
        }
        if (this.constraints.size() != (otherTable.constraints.size())) {
            differences.append(String.format("제약조건 개수가 다릅니다: %s <-> %s%n", this.constraints.size(), otherTable.constraints.size()));
        }

        for (Column column : this.columns) {
            boolean found = false;
            for (Column otherColumn : otherTable.columns) {
                if (column.getName().equals(otherColumn.getName())) {
                    found = true;
                    String columnDifferences = column.compare(otherColumn);
                    if (!columnDifferences.isEmpty()) {
                        differences.append(String.format("컬럼 [%s] 차이점: %s%n", column.getName(), columnDifferences));
                    }
                    break;
                }
            }
            if (!found) {
                differences.append(String.format("컬럼 [%s]이/가 다른 테이블에 존재하지 않습니다.%n", column.getName()));
            }
        }

        for (Constraint constraint : this.constraints) {
            boolean found = false;
            for (Constraint otherConstraint : otherTable.constraints) {
                if (constraint.getKeyName().equals(otherConstraint.getKeyName())) {
                    found = true;
                    String constraintDifferences = constraint.compare(otherConstraint);
                    if (!constraintDifferences.isEmpty()) {
                        differences.append(String.format("제약조건 [%s] 차이점: %s%n", constraint.getKeyName(), constraintDifferences));
                    }
                    break;
                }
            }
            if (!found) {
                differences.append(String.format("제약조건 [%s]이/가 다른 테이블에 존재하지 않습니다.%n", constraint.getKeyName()));
            }
        }

        return differences.toString();
    }
}
