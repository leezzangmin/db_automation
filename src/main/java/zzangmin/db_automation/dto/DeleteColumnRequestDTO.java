package zzangmin.db_automation.dto;


// rename -> delete

import zzangmin.db_automation.entity.Column;

public class DeleteColumnRequestDTO extends DDLRequestDTO {
    private String schemaName;
     private String tableName;
     private Column column;
}
