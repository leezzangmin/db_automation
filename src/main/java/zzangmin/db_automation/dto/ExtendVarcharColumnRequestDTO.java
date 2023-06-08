package zzangmin.db_automation.dto;

import zzangmin.db_automation.entity.Column;

public class ExtendVarcharColumnRequestDTO extends DDLRequestDTO {
    private String schemaName;
    private String tableName;
    private Column column;
    private short toBeLength;
}
