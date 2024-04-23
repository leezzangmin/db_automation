package zzangmin.db_automation.entity;


import lombok.extern.slf4j.Slf4j;

import java.util.*;

// https://jojoldu.tistory.com/137
@Slf4j
public enum DatabaseRequestCommandGroup {
    DDL("ddl",
            List.of(CommandType.CREATE_INDEX,
                    CommandType.CREATE_TABLE,
                    CommandType.ADD_COLUMN,
                    CommandType.ALTER_COLUMN,
                    CommandType.DELETE_COLUMN,
                    CommandType.EXTEND_VARCHAR_COLUMN,
                    CommandType.RENAME_COLUMN)),
    DML("dml",
            List.of(CommandType.INSERT,
                    CommandType.UPDATE,
                    CommandType.DELETE)),
    SELECT("select",
            List.of(CommandType.SELECT)),
    MIGRATION("migration",
            List.of(CommandType.TABLE_MIGRATION,
                    CommandType.DATABASE_MIGRATION)),
    PARAMETER("parameter",
            List.of(CommandType.PARAMETER_STANDARD,
                    CommandType.SCHEMA_STANDARD)),
    METRIC("metric",
            List.of(CommandType.CPU_METRIC,
                    CommandType.MEMORY_METRIC,
                    CommandType.HLL_METRIC)),
    SCHEMA_OBJECT("schema_object",
            List.of(CommandType.PROCEDURE,
                    CommandType.FUNCTION,
                    CommandType.VIEW,
                    CommandType.TRIGGER)),
    EMPTY("없음",
            List.of(CommandType.EMPTY));

    private String groupName;
    private List<CommandType> commandTypes = new ArrayList<>();

    DatabaseRequestCommandGroup(String groupName, List<CommandType> CommandTypes) {
        this.groupName = groupName;
        this.commandTypes = CommandTypes;
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
        PROCEDURE,
        FUNCTION,
        VIEW,
        TRIGGER,
        EMPTY
    }

    public static DatabaseRequestCommandGroup findDatabaseRequestCommandGroup(CommandType searchTarget) {
        return Arrays.stream(DatabaseRequestCommandGroup.values())
                .filter(group -> hasDatabaseRequestCommandOption(group, searchTarget))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException());
    }

    public static CommandType findCommandTypeByCommandTypeName(String searchTargetCommandTypeName) {
        CommandType findCommandType = Arrays.stream(CommandType.values())
                .filter(commandType -> commandType.name().equals(searchTargetCommandTypeName))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException())
        log.info("findCommandType: {}", findCommandType);
        return findCommandType;
    }

    public static List<CommandType> findDatabaseRequestCommandTypes(DatabaseRequestCommandGroup targetGroup) {
        if (targetGroup == null) {
            return DatabaseRequestCommandGroup.EMPTY.commandTypes;
        }
        return Arrays.stream(DatabaseRequestCommandGroup.values())
                .filter(group -> Objects.equals(group.name(),targetGroup.name()))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException())
                .commandTypes;
    }

    public static DatabaseRequestCommandGroup findDatabaseRequestCommandGroupByName(String targetGroupName) {
        log.info("targetGroupName: {}", targetGroupName);
        DatabaseRequestCommandGroup findGroup = Arrays.stream(DatabaseRequestCommandGroup.values())
                .filter(group -> Objects.equals(group.name(), targetGroupName))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException());
        log.info("findGroup: {}", findGroup);
        return findGroup;
    }

    private static boolean hasDatabaseRequestCommandOption(DatabaseRequestCommandGroup from, CommandType searchTarget) {
        return from.commandTypes
                .stream()
                .anyMatch(commandType -> commandType == searchTarget);
    }
}

