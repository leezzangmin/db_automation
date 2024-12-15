package zzangmin.db_automation.convention;

import zzangmin.db_automation.entity.mysqlobject.Constraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class IndexConvention {

    // https://dev.mysql.com/doc/refman/5.7/en/create-index.html

    public static List<String> validateIndexConvention(Constraint constraint) {
        List<String> errors = new ArrayList<>();
        if (!constraint.getConstraintType().equals(Constraint.ConstraintType.PRIMARY)) {
            errors.addAll(CommonConvention.validateBlankStr(constraint.getKeyName()));
            errors.addAll(CommonConvention.validateReservedWord(constraint.getKeyName()));
            errors.addAll(CommonConvention.validateSnakeCase(constraint.getKeyName()));
            errors.addAll(CommonConvention.validateLowerCaseString(constraint.getKeyName()));
            errors.addAll(validateConstraintNamingConvention(constraint.getKeyName(), constraint.getKeyColumnNames()));
        }
        errors.addAll(validateDuplicateColumnConvention(constraint.getKeyColumnNames()));
        errors.addAll(checkConstraintType(constraint.getConstraintType().typeName));
        return errors;
    }

    public static List<String> validateConstraintNamingConvention(String indexName, List<String> columnNames) {
        List<String> errors = new ArrayList<>();
        if (!String.join("_", columnNames).equals(indexName)) {
            errors.add(indexName + " 키 이름이 컬럼을 '_' 으로 이어붙인 형식이 아닙니다.");
        }
        return errors;
    }

    private static List<String> validateDuplicateColumnConvention(List<String> columnNames) {
        List<String> errors = new ArrayList<>();
        Set<String> columnNameSet = new HashSet<>();
        for (String columnName : columnNames) {
            if (columnNameSet.contains(columnName)) {
                errors.add("중복된 컬럼명이 존재합니다: " + columnName);
            }
            columnNameSet.add(columnName);
        }
        return errors;
    }

    private static List<String> checkConstraintType(String constraintType) {
        List<String> errors = new ArrayList<>();
        if (!CommonConvention.ALLOWED_CONSTRAINT_TYPE.contains(constraintType)) {
            errors.add("허용된 Constraint Type 이 아닙니다. [" + constraintType + "], 허용된 타입: " + CommonConvention.ALLOWED_CONSTRAINT_TYPE);
        }
        return errors;
    }

}
