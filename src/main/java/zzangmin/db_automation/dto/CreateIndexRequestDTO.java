package zzangmin.db_automation.dto;

import zzangmin.db_automation.entity.Column;

import java.util.List;

public class CreateIndexRequestDTO extends DDLRequestDTO {
        private String schemaName;
        private String tableName;
        private String indexName;
        private List<Column> columns;
}
