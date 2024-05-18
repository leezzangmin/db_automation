package zzangmin.db_automation.slackview;

import java.lang.reflect.Field;

public class SlackConstants {

    private SlackConstants() {}

    public static class CommandBlockIds {
        private CommandBlockIds() {}

        public static boolean isMember(String id) {
            Field[] fields = SlackConstants.CommandBlockIds.class.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldValue = (String) field.get(null);
                    if (id.equals(fieldValue) || id.startsWith(fieldValue)) {
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        // cluster schema table
        public static final String findClusterSelectsElementActionId = "selectClusterName";
        public static final String findTableSelectsElementActionId = "selectTableName";
        public static final String findSchemaSelectsElementActionId = "selectSchemaName";
        public static final String tableSchemaContextId = "tableSchemaContext";
        public static final String tableSchemaTextId = "tableSchemaText";

        // create index
        public static final String createIndexIndexNameTextInputId = "inputCreateIndexIndexName";
        public static final String createIndexColumnNameTextInputId = "inputCreateIndexColumnName";
        public static final String createIndexAddColumnButtonId = "createIndexAddColumnButton";
        public static final String createIndexRemoveColumnButtonId = "createIndexRemoveColumnButton";
        public static final String findIndexTypeActionId = "selectIndexType";

        // create table
        public static final String createTableSQLTextInputId = "inputCreateTableSQL";

        // add column
        public static final String addColumnColumnNameTextInputId = "inputAddColumnColumnName";
        public static final String addColumnColumnTypeTextInputId = "inputAddColumnColumnType";
        public static final String addColumnColumnIsNullRadioId = "selectAddColumnIsNullRadio";
        public static final String addColumnColumnCommentTextInputId = "inputAddColumnColumnComment";
        public static final String addColumnColumnDefaultValueTextInputId = "inputAddColumnColumnDefaultValue";

        // delete column
        public static final String deleteColumnColumnNameTextInputId = "inputDeleteColumnColumnName";

        // rename column
        public static final String renameColumnOldColumnNameTextInputId = "inputRenameColumnOldColumnName";
        public static final String renameColumnNewColumnNameTextInputId = "inputRenameColumnNewColumnName";
    }
    public static class FixedBlockIds {
        private FixedBlockIds() {}

        public static final String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";
        public static final String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";

    }

    public static class ErrorBlockIds {
        public static final String errorMessageBlockId = "displayErrorBlockGlobal";

        private ErrorBlockIds() {}
    }

}

// TODO: enum + reflection 제거
//public class SlackConstants {
//
//    private SlackConstants() {}
//
//    public enum CommandBlockIds {
//        // Cluster schema table
//        FIND_CLUSTER_SELECTS_ELEMENT("selectClusterName"),
//        FIND_TABLE_SELECTS_ELEMENT("selectTableName"),
//        FIND_SCHEMA_SELECTS_ELEMENT("selectSchemaName"),
//        TABLE_SCHEMA_CONTEXT("tableSchemaContext"),
//        TABLE_SCHEMA_TEXT("tableSchemaText"),
//
//        // Create index
//        CREATE_INDEX_INDEX_NAME_TEXT_INPUT("inputCreateIndexIndexName"),
//        CREATE_INDEX_COLUMN_NAME_TEXT_INPUT("inputCreateIndexColumnName"),
//        CREATE_INDEX_ADD_COLUMN_BUTTON("createIndexAddColumnButton"),
//        CREATE_INDEX_REMOVE_COLUMN_BUTTON("createIndexRemoveColumnButton"),
//        FIND_INDEX_TYPE("selectIndexType"),
//
//        // Create table
//        CREATE_TABLE_SQL_TEXT_INPUT("inputCreateTableSQL"),
//
//        // Add column
//        ADD_COLUMN_COLUMN_NAME_TEXT_INPUT("inputAddColumnColumnName"),
//        ADD_COLUMN_COLUMN_TYPE_TEXT_INPUT("inputAddColumnColumnType"),
//        ADD_COLUMN_COLUMN_IS_NULL_RADIO("selectAddColumnIsNullRadio"),
//        ADD_COLUMN_COLUMN_COMMENT_TEXT_INPUT("inputAddColumnColumnComment"),
//        ADD_COLUMN_COLUMN_DEFAULT_VALUE_TEXT_INPUT("inputAddColumnColumnDefaultValue"),
//
//        // Delete column
//        DELETE_COLUMN_COLUMN_NAME_TEXT_INPUT("inputDeleteColumnColumnName"),
//
//        // Rename column
//        RENAME_COLUMN_OLD_COLUMN_NAME_TEXT_INPUT("inputRenameColumnOldColumnName"),
//        RENAME_COLUMN_NEW_COLUMN_NAME_TEXT_INPUT("inputRenameColumnNewColumnName");
//
//        private final String actionId;
//
//        CommandBlockIds(String actionId) {
//            this.actionId = actionId;
//        }
//
//        public String getActionId() {
//            return this.actionId;
//        }
//
//        public static boolean isMember(String id) {
//            for (CommandBlockIds value : CommandBlockIds.values()) {
//                if (id.equals(value.getActionId()) || id.startsWith(value.getActionId())) {
//                    return true;
//                }
//            }
//            return false;
//        }
//    }
//
//    public enum FixedBlockIds {
//        FIND_DATABASE_REQUEST_COMMAND_GROUP_SELECTS_ELEMENT("selectDatabaseRequestCommandGroup"),
//        FIND_COMMAND_TYPE_SELECTS_ELEMENT("selectDatabaseRequestCommandType");
//
//        private final String actionId;
//
//        FixedBlockIds(String actionId) {
//            this.actionId = actionId;
//        }
//
//        public String getActionId() {
//            return this.actionId;
//        }
//    }
//
//    public enum ErrorBlockIds {
//        ERROR_MESSAGE_BLOCK("displayErrorBlockGlobal");
//
//        private final String actionId;
//
//        ErrorBlockIds(String actionId) {
//            this.actionId = actionId;
//        }
//
//        public String getActionId() {
//            return this.actionId;
//        }
//    }
//
//}
