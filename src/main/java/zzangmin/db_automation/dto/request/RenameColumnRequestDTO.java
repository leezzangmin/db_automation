package zzangmin.db_automation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RenameColumnRequestDTO extends DDLRequestDTO {
    private String schemaName;
    private String tableName;
    private String beforeColumnName;
    private String afterColumnName;
}
