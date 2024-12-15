package zzangmin.db_automation.convention;


import zzangmin.db_automation.entity.mysqlobject.Column;

import java.util.ArrayList;
import java.util.List;

import static zzangmin.db_automation.convention.CommonConvention.*;


public class ColumnConvention {

    private final static int SWITCH_STANDARD_BYTE = 255;

    public static List<String> validateColumnConvention(Column column) {
        List<String> errors = new ArrayList<>();
        errors.addAll(validateColumnNamingConvention(column.getName()));
        errors.addAll(validateColumnOption(column));
        errors.addAll(checkColumnCommentExistConvention(column.getName(), column.getComment()));
        return errors;
    }

    public static List<String> validateColumnNamingConvention(String columnName) {
        List<String> errors = new ArrayList<>();
        errors.addAll(CommonConvention.validateBlankStr(columnName));
        errors.addAll(CommonConvention.validateReservedWord(columnName));
        errors.addAll(CommonConvention.validateSnakeCase(columnName));
        errors.addAll(CommonConvention.validateLowerCaseString(columnName));
        return errors;
    }

    public static void validateExtendVarcharConvention(Column column, int futureLength) {
        // 255 바이트 기준으로 1byte -> 2byte
        int currentLength = column.injectVarcharLength();
        int currentByte = BYTE_PER_CHARACTER * currentLength;
        int futureByte = BYTE_PER_CHARACTER * futureLength;
        if (currentByte >= futureByte) {
            throw new IllegalArgumentException("varchar 컬럼을 축소하는 연산은 지원하지 않습니다.");
        }
        if (currentByte > SWITCH_STANDARD_BYTE) {
            return;
        }
        if (futureByte > SWITCH_STANDARD_BYTE) {
            throw new IllegalArgumentException("in-place 로 처리될 수 없는 extend 요청입니다.");
        }
    }

    private static List<String> validateColumnOption(Column column) {
        List<String> errors = new ArrayList<>();
        if (column.getType().startsWith("varchar") || column.getType().startsWith("VARCHAR")) {
            errors.addAll(column.validateCharType());
        }
        if (column.getType().contains("char") || column.getType().contains("CHAR") || column.getType().contains("text") || column.getType().contains("TEXT")) {
            if (column.getCharset() == null || !column.getCharset().equals(CHARSET)) {
                errors.add(column.getName() + " 의 CHARSET 이 " + CHARSET + " 이 아닙니다.");
//                throw new IllegalArgumentException(column.getName() + " 의 CHARSET 이 " + CHARSET + " 이 아닙니다.");
            }
            if (column.getCollate() == null || !column.getCollate().equals(COLLATE)) {
                errors.add(column.getName() + " 의 COLLATE 가 " + COLLATE + " 이 아닙니다.");
//                throw new IllegalArgumentException(column.getName() + " 의 COLLATE 가 " + COLLATE + " 이 아닙니다.");
            }
        }
        if (column.getIsAutoIncrement()) {
            if (column.getDefaultValue() != null && !column.getDefaultValue().isBlank()) {
                errors.add("auto_increment column 은 default value 를 가질 수 없습니다.");
//                throw new IllegalArgumentException("auto_increment column 은 default value 를 가질 수 없습니다.");
            }
        }
        return errors;
    }

    private static List<String> checkColumnCommentExistConvention(String columnName, String columnComment) {
        List<String> errors = new ArrayList<>();
        if (columnComment == null || columnComment.isBlank() || columnComment.isEmpty()) {
            errors.add(columnName + " 컬럼의 코멘트가 존재하지 않습니다.");
        }
        return errors;
    }

}
