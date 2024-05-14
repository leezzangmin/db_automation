package zzangmin.db_automation.convention;


import zzangmin.db_automation.entity.Column;

import static zzangmin.db_automation.convention.CommonConvention.*;


public class ColumnConvention {

    private final static int SWITCH_STANDARD_BYTE = 255;

    public static void validateColumnConvention(Column column) {
        validateColumnNamingConvention(column.getName());
        validateColumnOption(column);
        checkColumnCommentExistConvention(column.getName(), column.getComment());
    }

    public static void validateColumnNamingConvention(String columnName) {
        CommonConvention.validateSnakeCase(columnName);
        CommonConvention.validateLowerCaseString(columnName);
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

    private static void validateColumnOption(Column column) {
        if (column.getType().startsWith("varchar") || column.getType().startsWith("VARCHAR")) {
            column.injectVarcharLength();
        }
        if (column.getType().contains("char") || column.getType().contains("CHAR") || column.getType().contains("text") || column.getType().contains("TEXT")) {
//            if (!column.getCharset().equals(CHARSET)) {
//                throw new IllegalArgumentException(column.getName() + " 의 CHARSET 이 " + CHARSET + " 이 아닙니다.");
//            }
            if (!column.getCollate().equals(COLLATE)) {
                throw new IllegalArgumentException(column.getName() + " 의 COLLATE 가 " + COLLATE + " 이 아닙니다.");
            }
        }
    }

    private static void checkColumnCommentExistConvention(String columnName, String columnComment) {
        if (columnComment.isBlank() || columnComment.isEmpty()) {
            throw new IllegalArgumentException(columnName + " 의 코멘트가 존재하지 않습니다.");
        }
    }

}
