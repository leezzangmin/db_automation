package zzangmin.db_automation.entity;

public enum CommandType {
    CREATE_INDEX,
    CREATE_TABLE,
    ADD_COLUMN,
    ALTER_COLUMN,
    DELETE_COLUMN,
    EXTEND_VARCHAR_COLUMN,
    RENAME_COLUMN,
    RENAME_INDEX,
    ALTER_COLUMN_COMMENT,
    ALTER_TABLE_COMMENT,
}
