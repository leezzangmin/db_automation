package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;

@ToString
@Getter
@NoArgsConstructor
public class AlterColumnRequestDTO extends DDLRequestDTO {
        @NotBlank
        private CommandType commandType;
        @NotBlank
        private String schemaName;
        @NotBlank
        private String tableName;
        @NotBlank
        private String columnName;
        // 변경 후 컬럼 타입
        @NotBlank
        private Column afterColumn;
}
