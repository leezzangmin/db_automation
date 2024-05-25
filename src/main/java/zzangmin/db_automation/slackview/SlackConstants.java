package zzangmin.db_automation.slackview;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SlackConstants {

    private SlackConstants() {}

    public static class CommandBlockIds {
        private CommandBlockIds() {}

        public static boolean isMember(String id) {
            Class<?>[] innerClasses = CommandBlockIds.class.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                Field[] fields = innerClass.getDeclaredFields();
                for (Field field : fields) {
                    try {
                        String fieldValue = (String) field.get(null);
                        if (fieldValue.equals(id) || id.startsWith(fieldValue)) {
                            return true;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }

        public static List<String> getMembers(Class<?> clazz) {
            List<String> memberValues = new ArrayList<>();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldValue = (String) field.get(null);
                    memberValues.add(fieldValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return memberValues;
        }


        public static class ClusterSchemaTable {
            public static final String findClusterSelectsElementActionId = "selectClusterName";
            public static final String findTableSelectsElementActionId = "selectTableName";
            public static final String findSchemaSelectsElementActionId = "selectSchemaName";
            public static final String tableSchemaContextId = "tableSchemaContext";
            public static final String tableSchemaTextId = "tableSchemaText";
        }

        public static class CreateIndex {
            public static final String createIndexIndexNameTextInputId = "inputCreateIndexIndexName";
            public static final String createIndexColumnNameTextInputId = "inputCreateIndexColumnName";
            public static final String createIndexAddColumnButtonId = "createIndexAddColumnButton";
            public static final String createIndexRemoveColumnButtonId = "createIndexRemoveColumnButton";
            public static final String findIndexTypeActionId = "selectIndexType";
        }

        public static class CreateTable {
            public static final String createTableSQLTextInputId = "inputCreateTableSQL";
        }

        // add column
        public static class AddColumn {
            public static final String addColumnColumnNameTextInputId = "inputAddColumnColumnName";
            public static final String addColumnColumnTypeTextInputId = "inputAddColumnColumnType";
            public static final String addColumnColumnIsNullRadioId = "selectAddColumnIsNullRadio";
            public static final String addColumnColumnCommentTextInputId = "inputAddColumnColumnComment";
            public static final String addColumnColumnDefaultValueTextInputId = "inputAddColumnColumnDefaultValue";
        }

        // delete column
        public static class DeleteColumn {
            public static final String deleteColumnColumnNameTextInputId = "inputDeleteColumnColumnName";
        }


        // rename column
        public static class RenameColumn {
            public static final String renameColumnOldColumnNameTextInputId = "inputRenameColumnOldColumnName";
            public static final String renameColumnNewColumnNameTextInputId = "inputRenameColumnNewColumnName";
        }

        // rename table
        public static class RenameTable {
            public static final String renameTableNewTableNameTextInputId = "inputRenameTableNewTableName";
        }

        // show grant
        public static class ShowGrant {
            public static final String selectMysqlAccountSelectBlockId = "selectMysqlAccountSelectBlockId";
            public static final String findAccountListButtonBlockId = "findAccountListButtonBlockId";
        }
    }

    public static class FixedBlockIds {

        private FixedBlockIds() {}

        public static boolean isMember(String id) {
            Field[] fields = SlackConstants.FixedBlockIds.class.getDeclaredFields();
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

        public static final String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";
        public static final String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";

    }

    public static class CommunicationBlockIds {
        public static final String commandRequestAcceptDenyButtonBlockId = "commandRequestAcceptDenyButtonBlockId";
        public static final String commandRequestAcceptButtonBlockId = "commandRequestAcceptButtonBlockId";
        public static final String commandRequestDenyButtonBlockId = "commandRequestDenyButtonBlockId";

        private CommunicationBlockIds() {}
    }

    public static class ErrorBlockIds {
        public static final String errorMessageBlockId = "displayErrorBlockGlobal";

        private ErrorBlockIds() {}
    }

}
