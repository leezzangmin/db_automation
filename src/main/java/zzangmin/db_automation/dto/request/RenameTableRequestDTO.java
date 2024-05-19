package zzangmin.db_automation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RenameTableRequestDTO implements DDLRequestDTO {

    private String schemaName;
    private String oldTableName;
    private String newTableName;

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ");
        sb.append(schemaName);
        sb.append(".");
        sb.append(oldTableName);
        sb.append(" RENAME TO ");
        sb.append(schemaName);
        sb.append(".");
        sb.append(newTableName);
        sb.append(";");
        return sb.toString();
    }


    @Override
    public DatabaseRequestCommandGroup.CommandType getCommandType() {
        return DatabaseRequestCommandGroup.CommandType.RENAME_TABLE;
    }
}
