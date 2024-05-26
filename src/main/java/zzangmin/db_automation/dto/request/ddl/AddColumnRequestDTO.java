package zzangmin.db_automation.dto.request.ddl;

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
public class AddColumnRequestDTO implements DDLRequestDTO {

      @NotBlank
      private String schemaName;
      @NotBlank
      private String tableName;
      @NotBlank
      private Column column;

      @Override
      public String toSQL() {
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE `");
            sb.append(this.getSchemaName());
            sb.append("`.`");
            sb.append(this.getTableName());
            sb.append("` ADD COLUMN `");
            sb.append(this.getColumn().getName());
            sb.append("` ");
            sb.append(this.getColumn().getType());
            sb.append(" ");
            sb.append(this.getColumn().generateNull());
            sb.append(" COMMENT '");
            sb.append(this.getColumn().getComment());
            sb.append("'");
            return sb.toString();
      }

      @Override
      public DatabaseRequestCommandGroup.CommandType extractCommandType() {
            return DatabaseRequestCommandGroup.CommandType.ADD_COLUMN;
      }
}
