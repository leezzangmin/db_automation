package zzangmin.db_automation.convention;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.Column;

import static zzangmin.db_automation.convention.CommonConvention.*;

@RequiredArgsConstructor
@Component
public class ColumnConvention {

    private final CommonConvention commonConvention;
    private final static int SWITCH_STANDARD_BYTE = 255;

    // 255 바이트 기준으로 1byte -> 2byte
    public void validateExtendVarcharConvention(Column column, int futureLength) {
        int currentLength = column.getVarcharLength();
        int currentByte = BYTE_PER_CHARACTER * currentLength;
        int futureByte = BYTE_PER_CHARACTER * futureLength;
        if (currentByte > SWITCH_STANDARD_BYTE) {
            return;
        }
        if (futureByte > SWITCH_STANDARD_BYTE) {
            throw new IllegalArgumentException("in-place 로 처리될 수 없는 extend 요청입니다.");
        }
        if (currentByte >= futureByte) {
            throw new IllegalArgumentException("varchar 컬럼을 축소하는 연산은 지원하지 않습니다.");
        }
    }

    public void validateColumnConvention(Column column) {
        validateColumnNamingConvention(column.getName());
        validateColumnOption(column.getName(), column.getCharset(), column.getCollate());
        checkColumnCommentExistConvention(column.getName(), column.getComment());
        checkUniqueNullable(column.isUnique(), column.isNull(), column.getDefaultValue());
    }

    private void validateColumnNamingConvention(String columnName) {
        commonConvention.validateSnakeCase(columnName);
        commonConvention.validateLowerCaseString(columnName);
    }

    private void validateColumnOption(String columnName, String columnCharset, String columnCollate) {
        if (!columnCharset.equals(CHARSET)) {
            throw new IllegalArgumentException(columnName + " 의 CHARSET 이 " + CHARSET +" 이 아닙니다.");
        }
        if (!columnCollate.equals(COLLATE)) {
            throw new IllegalArgumentException(columnName + " 의 COLLATE 가 " + COLLATE +" 이 아닙니다.");
        }
    }

    private void checkColumnCommentExistConvention(String columnName, String columnComment) {
        if (columnComment.isBlank() || columnComment.isEmpty()) {
            throw new IllegalArgumentException(columnName + " 의 코멘트가 존재하지 않습니다.");
        }
    }
    
    private void checkUniqueNullable(boolean isUnique, boolean isNull, String defaultValue) {
        if (isUnique && isNull) {
            // TODO: unique + not null 은 빈 테이블만 수행 가능함. 여기서 체크하는게 맞나
        }
    }
}
