package zzangmin.db_automation.convention;

import zzangmin.db_automation.entity.mysqlobject.Column;
import zzangmin.db_automation.entity.mysqlobject.Constraint;
import zzangmin.db_automation.entity.mysqlobject.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static zzangmin.db_automation.convention.CommonConvention.*;


public class TableConvention {

    /**
     * 0. 테이블, 컬럼, 인덱스 등 네이밍 컨벤션 (+영어만 허용, 공백X, snake_case)
     * 1. 중복된 옵션(column, constraint) 있는지
     * 2. 테이블 생성 컨벤션 (engine charset, comment 등)
     */
//
//    public static void validateTableConvention(Table table) {
//        checkDuplicateColumnAndConstraintConvention(table.getColumns(), table.getConstraints());
//        checkNamingConvention(table.getColumns(), table.getConstraints(), table.getTableName());
//        checkTableOptionConvention(table.getTableEngine(), table.getTableCharset(), table.getTableCollate(), table.getTableComment());
//        for (Column column : table.getColumns()) {
//            ColumnConvention.validateColumnConvention(column);
//        }
//        for (Constraint constraint : table.getConstraints()) {
//            IndexConvention.validateIndexConvention(constraint);
//        }
//    }

    public static List<String> validateTableConvention(Table table) {
        List<String> errors = new ArrayList<>();
        errors.addAll(checkDuplicateColumnAndConstraintConvention(table.getColumns(), table.getConstraints()));
        errors.addAll(checkNamingConvention(table.getColumns(), table.getConstraints(), table.getTableName()));
        errors.addAll(checkTableOptionConvention(table.getTableEngine(), table.getTableCharset(), table.getTableCollate(), table.getTableComment()));
        for (Column column : table.getColumns()) {
            errors.addAll(ColumnConvention.validateColumnConvention(column));
        }
        for (Constraint constraint : table.getConstraints()) {
            errors.addAll(IndexConvention.validateIndexConvention(constraint));
        }
        return errors;
    }


    private static List<String> checkNamingConvention(Set<Column> columns, Set<Constraint> constraints, String tableName) {
        List<String> errors = new ArrayList<>();
        errors.addAll(ColumnConvention.validateColumnNamingConvention(tableName));
        for (Column column : columns) {
            errors.addAll(CommonConvention.validateReservedWord(column.getName()));
            errors.addAll(CommonConvention.validateSnakeCase(column.getName()));
            errors.addAll(CommonConvention.validateLowerCaseString(column.getName()));
        }
        for (Constraint constraint : constraints) {
            if (constraint.getConstraintType().equals(Constraint.ConstraintType.PRIMARY)) {
                continue;
            }
            errors.addAll(CommonConvention.validateReservedWord(constraint.getKeyName()));
            errors.addAll(CommonConvention.validateSnakeCase(constraint.getKeyName()));
            errors.addAll(CommonConvention.validateLowerCaseString(constraint.getKeyName()));
        }
        return errors;
    }

    private static List<String> checkDuplicateColumnAndConstraintConvention(Set<Column> columns, Set<Constraint> constraints) {
        List<String> errors = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();
        for (Column column : columns) {
            String columnName = column.getName();
            if (columnNames.contains(columnName)) {
                errors.add("중복된 컬럼명이 존재합니다: " + columnName);
            }
            columnNames.add(columnName);
        }

        Set<String> constraintNames = new HashSet<>();
        for (Constraint constraint : constraints) {
            String constraintName = constraint.getKeyName();
            if (constraintNames.contains(constraintName)) {
                errors.add("중복된 키 이름이 존재합니다: " + constraintName);
            }
            constraintNames.add(constraintName);
        }
        return errors;
    }

    private static List<String> checkTableOptionConvention(String tableEngine, String tableCharset, String tableCollate, String tableComment) {
        List<String> errors = new ArrayList<>();
        if (tableEngine == null || !tableEngine.equals(ENGINE_TYPE)) {
            errors.add("테이블 엔진은 다음과 같아야합니다: " + ENGINE_TYPE);
        }
        if (tableCharset == null || !tableCharset.equals(CHARSET)) {
            errors.add("테이블 캐릭터셋은 다음과 같아야합니다: " + CHARSET);
        }
        if (tableCollate == null || !tableCollate.equals(COLLATE)) {
            errors.add("테이블 콜레이션은 다음과 같아야합니다: " + COLLATE);
        }
        if (tableComment == null || tableComment.isBlank() || tableComment.isEmpty()) {
            errors.add("테이블 코멘트가 존재하지 않습니다.");
        }
        return errors;
    }


}
