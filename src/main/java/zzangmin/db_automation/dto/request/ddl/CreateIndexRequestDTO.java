package zzangmin.db_automation.dto.request.ddl;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import zzangmin.db_automation.entity.mysqlobject.Constraint;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIndexRequestDTO implements DDLRequestDTO {
        @NotBlank
        private String schemaName;
        @NotBlank
        private String tableName;
        @NotBlank
        private String indexName;
        @NotBlank
        private String indexType;
        @NotBlank
        private List<String> columnNames;

        public Constraint toConstraint() {
                return new Constraint(Constraint.ConstraintType.valueOf(this.getIndexType()),
                        this.getIndexName(),
                        this.getColumnNames());
        }

        @Override
        public String toSQL() {
                StringBuilder sb = new StringBuilder();
                sb.append("ALTER TABLE `");
                sb.append(this.getSchemaName());
                sb.append("`.`");
                sb.append(this.getTableName());
                if (this.getIndexType().equals("UNIQUE")) {
                        sb.append("` ADD UNIQUE INDEX `");
                } else {
                        sb.append("` ADD INDEX `");
                }
                sb.append(this.getIndexName());
                sb.append("` (");
                for (String columnName : this.getColumnNames()) {
                        sb.append("`");
                        sb.append(columnName);
                        sb.append("`,");
                }
                sb.deleteCharAt(sb.lastIndexOf(","));
                sb.append(")");
                return sb.toString();
        }

        @Override
        public DatabaseRequestCommandGroup.CommandType extractCommandType() {
                return DatabaseRequestCommandGroup.CommandType.CREATE_INDEX;
        }
}
