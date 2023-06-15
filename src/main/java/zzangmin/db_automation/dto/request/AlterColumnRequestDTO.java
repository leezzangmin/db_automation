package zzangmin.db_automation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.CommandType;

@ToString
@Getter
@NoArgsConstructor
public class AlterColumnRequestDTO extends DDLRequestDTO {
        private CommandType commandType;
        private String schemaName;
        private String tableName;
        private String columnName;

        // 변경 후 컬럼 타입
        private String toBeColumnType;
}
