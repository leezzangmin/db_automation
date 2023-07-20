package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlterColumnRequestDTO extends DDLRequestDTO {

        @NotBlank
        private String schemaName;
        @NotBlank
        private String tableName;
        @NotBlank
        private String targetColumnName;
        @NotBlank
        private Column afterColumn;
}
