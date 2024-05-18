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
public class AlterColumnRequestDTO implements DDLRequestDTO {

        @NotBlank
        private String schemaName;
        @NotBlank
        private String tableName;
        @NotBlank
        private String targetColumnName;
        @NotBlank
        private Column afterColumn;

        @Override
        public String toSQL() {
                StringBuilder sb = new StringBuilder();
                sb.append("ALTER TABLE `");
                sb.append(this.getSchemaName());
                sb.append("`.`");
                sb.append(this.getTableName());
                sb.append("` MODIFY COLUMN `");
                sb.append(this.getTargetColumnName());
                sb.append("` ");
                sb.append(this.getAfterColumn().getType());
                sb.append(" ");
                sb.append(this.getAfterColumn().generateNull());
                sb.append(this.getAfterColumn().generateAutoIncrement());
                sb.append(" COMMENT '");
                sb.append(this.getAfterColumn().getComment());
                sb.append("'");
                return sb.toString();
        }

        @Override
        public DatabaseRequestCommandGroup.CommandType getCommandType() {
                return DatabaseRequestCommandGroup.CommandType.ALTER_COLUMN;
        }
}
