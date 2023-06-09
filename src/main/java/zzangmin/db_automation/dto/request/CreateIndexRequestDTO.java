package zzangmin.db_automation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class CreateIndexRequestDTO implements DDLRequestDTO {
        private CommandType commandType;
        private String schemaName;
        private String tableName;
        private String indexName;
        private List<Column> columns;
}
