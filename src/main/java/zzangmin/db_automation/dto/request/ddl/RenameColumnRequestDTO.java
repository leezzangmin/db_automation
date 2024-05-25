package zzangmin.db_automation.dto.request.ddl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RenameColumnRequestDTO implements DDLRequestDTO {
    private String schemaName;
    private String tableName;
    private String beforeColumnName;
    private String afterColumnName;

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(this.getSchemaName());
        sb.append("`.`");
        sb.append(this.getTableName());
        sb.append("` RENAME COLUMN `");
        sb.append(this.getBeforeColumnName());
        sb.append("` TO `");
        sb.append(this.getAfterColumnName());
        sb.append("`");
        return sb.toString();
    }

    @Override
    public DatabaseRequestCommandGroup.CommandType getCommandType() {
        return DatabaseRequestCommandGroup.CommandType.RENAME_COLUMN;
    }
}
