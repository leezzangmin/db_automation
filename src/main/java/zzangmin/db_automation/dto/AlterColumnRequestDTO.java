package zzangmin.db_automation.dto;


public class AlterColumnRequestDTO extends DDLRequestDTO {
        private String schemaName;
        private String tableName;
        private String columnName;

        // 변경 후 컬럼 타입
        private String toBeColumnType;
}
