package zzangmin.db_automation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;

@ToString
@Getter
@NoArgsConstructor
public class ExtendVarcharColumnRequestDTO extends DDLRequestDTO {
    private CommandType commandType;
    private String schemaName;
    private String tableName;
    private Column column;
}
