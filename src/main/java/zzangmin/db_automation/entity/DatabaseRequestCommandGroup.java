package zzangmin.db_automation.entity;


import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// https://jojoldu.tistory.com/137
@Slf4j
public enum DatabaseRequestCommandGroup {
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
    }),
    EMPTY("없음", new CommandType[]{
            CommandType.EMPTY
    });

    private String groupName;
    private CommandType[] commandTypes;

    DatabaseRequestCommandGroup(String groupName, CommandType[] CommandType) {
        this.groupName = groupName;
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
        HLL_METRIC,
        EMPTY
    }

    public static DatabaseRequestCommandGroup findDatabaseRequestCommandGroup(CommandType searchTarget) {
        return Arrays.stream(DatabaseRequestCommandGroup.values())
                .filter(group -> hasDatabaseRequestCommandOption(group, searchTarget))
                .findAny()
                .orElse(DatabaseRequestCommandGroup.EMPTY);
    }

    public static List<CommandType> findDatabaseRequestCommandTypes(DatabaseRequestCommandGroup targetGroup) {
        if (targetGroup == null) {
            return List.of(DatabaseRequestCommandGroup.EMPTY.commandTypes);
        }
        return Arrays.stream(Arrays.stream(DatabaseRequestCommandGroup.values())
                .filter(group -> group.groupName.equals(targetGroup.groupName))
                .findAny()
                .orElse(DatabaseRequestCommandGroup.EMPTY)
                .commandTypes).toList();
    }

    public static DatabaseRequestCommandGroup findDatabaseRequestCommandGroupByName(String targetGroupName) {
        log.info("targetGroupName: {}", targetGroupName);
        DatabaseRequestCommandGroup findGroup = Arrays.stream(DatabaseRequestCommandGroup.values())
                .filter(group -> Objects.equals(group.groupName, targetGroupName))
                .findAny()
                .orElse(DatabaseRequestCommandGroup.EMPTY);
        log.info("findGroup: {}", findGroup);
        return findGroup;
    }

    private static boolean hasDatabaseRequestCommandOption(DatabaseRequestCommandGroup from, CommandType searchTarget) {
        return Arrays.stream(from.commandTypes)
                .anyMatch(commandType -> commandType == searchTarget);
    }
}

