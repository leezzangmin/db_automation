package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import zzangmin.db_automation.entity.Constraint;

import java.util.List;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIndexRequestDTO extends DDLRequestDTO {
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
}
