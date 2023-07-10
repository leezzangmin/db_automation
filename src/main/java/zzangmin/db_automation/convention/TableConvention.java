package zzangmin.db_automation.convention;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.entity.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static zzangmin.db_automation.convention.CommonConvention.*;

@RequiredArgsConstructor
@Component
public class TableConvention {

    /**
     * 0. 테이블, 컬럼, 인덱스 등 네이밍 컨벤션 (+영어만 허용, 공백X, snake_case)
     * 1. 중복된 옵션(column, constraint) 있는지
     * 2. 테이블 생성 컨벤션 (engine charset, comment 등)
     */

    private final CommonConvention commonConvention;
    private final IndexConvention indexConvention;
    private final ColumnConvention columnConvention;

    public void validateTableConvention(Table table) {
        checkDuplicateColumnAndConstraintConvention(table.getColumns(), table.getConstraints());
        checkNamingConvention(table.getColumns(), table.getConstraints(), table.getTableName());
        checkTableOptionConvention(table.getTableEngine(), table.getTableCharset(), table.getTableCollate(), table.getTableComment());
        for (Column column : table.getColumns()) {
            columnConvention.validateColumnConvention(column);
        }
        for (Constraint constraint : table.getConstraints()) {
            indexConvention.validateIndexConvention(constraint);
        }
    }

    private void checkNamingConvention(List<Column> columns, List<Constraint> constraints, String tableName) {
        commonConvention.validateSnakeCase(tableName);
        commonConvention.validateLowerCaseString(tableName);
        for (Column column : columns) {
            commonConvention.validateSnakeCase(column.getName());
            commonConvention.validateLowerCaseString(column.getName());
        }
        for (Constraint constraint : constraints) {
            if (constraint.getType().equals("PRIMARY KEY")) {
                continue;
            }
            commonConvention.validateSnakeCase(constraint.getKeyName());
            commonConvention.validateLowerCaseString(constraint.getKeyName());
        }
    }

    private void checkDuplicateColumnAndConstraintConvention(List<Column> columns, List<Constraint> constraints) {
        Set<String> columnNames = new HashSet<>();
        for (Column column : columns) {
            String columnName = column.getName();
            if (columnNames.contains(columnName)) {
                throw new IllegalArgumentException("중복된 컬럼명이 존재합니다: " + columnName);
            }
            columnNames.add(columnName);
        }

        Set<String> constraintNames = new HashSet<>();
        for (Constraint constraint : constraints) {
            String constraintName = constraint.getKeyName();
            if (constraintNames.contains(constraintName)) {
                throw new IllegalArgumentException("중복된 키 이름이 존재합니다: " + constraintName);
            }
            constraintNames.add(constraintName);
        }
    }

    private void checkTableOptionConvention(String tableEngine, String tableCharset, String tableCollate, String tableComment) {
        if (!tableEngine.equals(ENGINE_TYPE)) {
            throw new IllegalArgumentException("테이블 엔진은 다음과 같아야합니다: " + ENGINE_TYPE);
        }
        if (!tableCharset.equals(CHARSET)) {
            throw new IllegalArgumentException("테이블 캐릭터셋은 다음과 같아야합니다: " + CHARSET);
        }
        if (!tableCollate.equals(COLLATE)) {
            throw new IllegalArgumentException("테이블 콜레이션은 다음과 같아야합니다: " + COLLATE);
        }
        if (tableComment.isBlank() || tableComment.isEmpty()) {
            throw new IllegalArgumentException("테이블 코멘트가 존재하지 않습니다.");
        }
    }


}
