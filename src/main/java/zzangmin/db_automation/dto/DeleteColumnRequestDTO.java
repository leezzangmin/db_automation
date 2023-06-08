package zzangmin.db_automation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;

@ToString
@Getter
@NoArgsConstructor
public class DeleteColumnRequestDTO implements DDLRequestDTO {
    private CommandType commandType;
    private String schemaName;
    private String tableName;
    private Column column;
}
