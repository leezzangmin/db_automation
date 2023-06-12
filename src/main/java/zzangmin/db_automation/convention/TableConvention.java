package zzangmin.db_automation.convention;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class TableConvention {

    /**
     * 0. 테이블, 컬럼, 인덱스 등 네이밍 컨벤션 (+영어만 허용, 공백X, snake_case)
     * 1. 중복된 옵션(column, constraint) 있는지
     * 2. 테이블 생성 컨벤션 (engine charset, comment 등)
     */
    private static final String ENGINE_TYPE = "InnoDB";
    private static final String CHARSET = "utf8mb4";
    private static final String COLLATE = "utf8mb4_general_ci";
    private static final Set<String> CONSTRAINT_TYPE = Set.of("PRIMARY KEY", "UNIQUE KEY", "KEY");
    private final CommonConvention commonConvention;
    private final IndexConvention indexConvention;


    public void validateTableConvention(List<Column> columns, List<Constraint> constraints, String tableName, String tableEngine, String tableCharset, String tableCollate, String tableComment) {
        checkDuplicateColumnAndConstraintConvention(columns, constraints);
        checkNamingConvention(columns, constraints, tableName);
        checkColumnCommentExistConvention(columns);
        checkTableOptionConvention(tableEngine, tableCharset, tableCollate, tableComment);
        checkConstraintType(constraints);
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
            indexConvention.validateConstraintNamingConvention(constraint.getKeyName(), constraint.getKeyColumnNames());
        }
    }

    private void checkColumnCommentExistConvention(List<Column> columns) {
        for (Column column : columns) {
            if (column.getComment().isBlank() || column.getComment().isEmpty()) {
                throw new IllegalArgumentException("테이블 코멘트가 존재하지 않습니다.");
            }
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
            throw new IllegalArgumentException("엔진명은 다음과 같아야합니다: " + ENGINE_TYPE);
        }
        if (!tableCharset.equals(CHARSET)) {
            throw new IllegalArgumentException("캐릭터셋은 다음과 같아야합니다: " + CHARSET);
        }
        if (!tableCollate.equals(COLLATE)) {
            throw new IllegalArgumentException("콜레이션은 다음과 같아야합니다: " + COLLATE);
        }
        if (tableComment.isBlank() || tableComment.isEmpty()) {
            throw new IllegalArgumentException("테이블 코멘트가 존재하지 않습니다.");
        }
    }

    private void checkConstraintType(List<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            if (!CONSTRAINT_TYPE.contains(constraint.getType())) {
                throw new IllegalArgumentException("허용된 Constraint Type 이 아닙니다. [" + constraint.getType() + "]");
            }
        }
    }


}
