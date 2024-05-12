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
        public static final String createTableTableNameTextInputId = "inputCreateTableTableName";
        public static final String createTableColumnNameTextInputId = "inputCreateTableColumnName";
        public static final String createTableColumnTypeTextInputId = "inputCreateTableColumnType";
        public static final String createTableColumnOptionTypeMultiSelectSectionId = " createTableColumnOptionTypeMultiSelect";
        public static final String createTableColumnDefaultValueTextInputId = "inputCreateTableColumnDefaultValue";

    }
    public static class FixedBlockIds {
        private FixedBlockIds() {}

        public static final String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";
        public static final String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";

    }

    public static class BlockIds {
        private BlockIds() {}
    }

}