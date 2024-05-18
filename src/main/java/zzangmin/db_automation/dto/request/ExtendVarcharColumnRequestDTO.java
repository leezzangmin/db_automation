package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExtendVarcharColumnRequestDTO implements DDLRequestDTO {

    @NotBlank
    private String schemaName;
    @NotBlank
    private String tableName;
    @NotBlank
    private Column oldColumn;
    @NotBlank
    private int extendSize;

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(this.getSchemaName());
        sb.append("`.`");
        sb.append(this.getTableName());
        sb.append("` MODIFY COLUMN `");
        sb.append(this.getOldColumn().getName());
        sb.append("` ");
        sb.append("varchar(");
        sb.append(this.extendSize);
        sb.append(") ");
        sb.append(this.getOldColumn().generateNull());
        sb.append(this.getOldColumn().generateAutoIncrement());
        sb.append(" COMMENT '");
        sb.append(oldColumn.getComment());
        sb.append("'");
        return sb.toString();
    }

    @Override
    public DatabaseRequestCommandGroup.CommandType getCommandType() {
        return DatabaseRequestCommandGroup.CommandType.EXTEND_VARCHAR_COLUMN;
    }
}
