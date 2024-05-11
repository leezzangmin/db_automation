package zzangmin.db_automation.slackview;

public class SlackConstants {

    private SlackConstants() {}

    public static class CommandBlockIds {
        private CommandBlockIds() {}

        public static final String createIndexIndexNameTextInputId = "inputCreateIndexIndexName";
        public static final String createIndexColumnNameTextInputId = "inputCreateIndexColumnName";
        public static final String createIndexAddColumnButtonId = "createIndexAddColumnButton";
        public static final String createIndexRemoveColumnButtonId = "createIndexRemoveColumnButton";
        public static final String findIndexTypeActionId = "selectIndexType";
        public static final String findClusterSelectsElementActionId = "selectClusterName";
        public static final String findTableSelectsElementActionId = "selectTableName";
        public static final String findSchemaSelectsElementActionId = "selectSchemaName";
        public static final String tableSchemaContextId = "tableSchemaContext";
        public static final String tableSchemaTextId = "tableSchemaText";
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