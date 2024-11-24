package zzangmin.db_automation.convention;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zzangmin.db_automation.entity.mysqlobject.Column;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ColumnConvention 테스트")
class ColumnConventionTest {


    @Test
    @DisplayName("컬럼 컨벤션 검증")
    void validateColumnConventionTest() {
        // Given
        Column column = Column.builder()
                .name("user_id")
                .type("varchar(255)")
                .isNull(true)
                .defaultValue(null)
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("User ID comment")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();

        // When & Then
        assertDoesNotThrow(() -> ColumnConvention.validateColumnConvention(column));
    }

    @Test
    @DisplayName("컬럼 컨벤션 검증 실패")
    void validateColumnConventionTestFail() {
        // Given
        Column column = Column.builder()
                .name("user_id1")
                .type("varchar(255)")
                .isNull(true)
                .defaultValue(null)
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("User ID comment")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ColumnConvention.validateColumnConvention(column));
    }

    @Test
    @DisplayName("auto increment 컬럼 default value 컨벤션 검증 실패")
    void validateColumnConventionIsAutoIncrementHasDefaultValue() {
        // Given
        Column column = Column.builder()
                .name("user_id")
                .type("varchar(255)")
                .isNull(true)
                .defaultValue("asdfasdf")
                .isAutoIncrement(true)
                .comment("User ID comment")
                .collate("utf8mb4_0900_ai_ci")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ColumnConvention.validateColumnConvention(column));
    }

    @Test
    @DisplayName("컬럼 네이밍 컨벤션 검증")
    void validateColumnNamingConvention() {
        // Given
        String columnName = "user_id";

        // When & Then
        assertDoesNotThrow(() -> ColumnConvention.validateColumnNamingConvention(columnName));
    }

    @Test
    @DisplayName("컬럼 네이밍 컨벤션 검증 실패")
    void validateColumnNamingConventionFail() {
        // Given
        String columnName = "123user_id";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ColumnConvention.validateColumnNamingConvention(columnName));
    }


    @Test
    @DisplayName("extend varchar 컨벤션 검증")
    void validateExtendVarcharConvention() {
        // Given
        Column column = Column.builder()
                .name("user_name")
                .type("varchar(100)")
                .isNull(true)
                .defaultValue(null)
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("User Name")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        int futureLength = 150;

        // When & Then
        assertDoesNotThrow(() -> ColumnConvention.validateExtendVarcharConvention(column, futureLength));
    }

    @Test
    @DisplayName("extend varchar 컨벤션 검증 실패")
    void validateExtendVarcharConventionFail() {
        // Given
        Column column = Column.builder()
                .name("user_name")
                .type("varchar(30)")
                .isNull(true)
                .defaultValue(null)
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("User Name")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        int futureLength = 256;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> ColumnConvention.validateExtendVarcharConvention(column, futureLength));
    }

    @Test
    @DisplayName("컬럼 코멘트 존재 여부 검증")
    void checkColumnCommentExistConvention() {
        // Given
        Column column = Column.builder()
                .name("user_email")
                .type("varchar(255)")
                .isNull(true)
                .defaultValue(null)
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ColumnConvention.validateColumnConvention(column));
        assertEquals("user_email 의 코멘트가 존재하지 않습니다.", exception.getMessage());
    }

}