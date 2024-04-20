package zzangmin.db_automation.entity;


// https://jojoldu.tistory.com/137
public enum DatabaseRequestCommandType {
    DDL("ddl", new CommandType[] {
            CommandType.CREATE_INDEX,
            CommandType.CREATE_TABLE,
            CommandType.ADD_COLUMN,
            CommandType.ALTER_COLUMN,
            CommandType.DELETE_COLUMN,
            CommandType.EXTEND_VARCHAR_COLUMN,
            CommandType.RENAME_COLUMN,
    }),
    DML("dml", new CommandType[] {
            CommandType.INSERT,
            CommandType.UPDATE,
            CommandType.DELETE,
    }),
    SELECT("select", new CommandType[] {
            CommandType.SELECT
    }),
    MIGRATION("migration", new CommandType[] {
            CommandType.TABLE_MIGRATION,
            CommandType.DATABASE_MIGRATION,
    }),
    PARAMETER("parameter", new CommandType[] {
            CommandType.PARAMETER_STANDARD,
            CommandType.SCHEMA_STANDARD,
    }),
    METRIC("metric", new CommandType[] {
            CommandType.CPU_METRIC,
            CommandType.MEMORY_METRIC,
            CommandType.HLL_METRIC
    });

    private String name;
    private CommandType[] commandTypes;

    DatabaseRequestCommandType(String name, CommandType[] CommandType) {
        this.name = name;
        this.commandTypes = CommandType;
    }

    public enum CommandType {
        CREATE_INDEX,
        CREATE_TABLE,
        ADD_COLUMN,
        ALTER_COLUMN,
        DELETE_COLUMN,
        EXTEND_VARCHAR_COLUMN,
        RENAME_COLUMN,
        INSERT,
        UPDATE,
        DELETE,
        SELECT,
        TABLE_MIGRATION,
        DATABASE_MIGRATION,
        PARAMETER_STANDARD,
        SCHEMA_STANDARD,
        CPU_METRIC,
        MEMORY_METRIC,
        HLL_METRIC
    }
}

