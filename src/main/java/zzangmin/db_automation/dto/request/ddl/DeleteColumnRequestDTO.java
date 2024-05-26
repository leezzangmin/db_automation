package zzangmin.db_automation.dto.request.ddl;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteColumnRequestDTO implements DDLRequestDTO {
    @NotBlank
    private String schemaName;
    @NotBlank
    private String tableName;
    @NotBlank
    private String columnName;

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(this.getSchemaName());
        sb.append("`.`");
        sb.append(this.getTableName());
        sb.append("` DROP COLUMN `");
        sb.append(this.getColumnName());
        sb.append("`");
        return sb.toString();
    }

    @Override
    public DatabaseRequestCommandGroup.CommandType extractCommandType() {
        return DatabaseRequestCommandGroup.CommandType.DELETE_COLUMN;
    }
}
