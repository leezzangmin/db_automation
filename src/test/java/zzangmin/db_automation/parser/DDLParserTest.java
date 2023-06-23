package zzangmin.db_automation.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zzangmin.db_automation.dto.request.AddColumnRequestDTO;
import zzangmin.db_automation.dto.request.DDLRequestDTO;
import zzangmin.db_automation.dto.request.RenameColumnRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;

import static org.junit.jupiter.api.Assertions.*;

class DDLParserTest {

    private DDLParser ddlParser = new DDLParser();

    @DisplayName("rename column command to sql 테스트")
    @Test
    void testRenameColumnCommandToSql() {
        // given
        DDLRequestDTO dto = new RenameColumnRequestDTO("test_schema", "test_table", "old_column", "new_column");
        dto.setCommandType(CommandType.RENAME_COLUMN);
        // when
        String sql = ddlParser.commandToSql(dto);

        // then
        String expectedSql = "ALTER TABLE `test_schema`.`test_table` RENAME COLUMN `old_column` TO `new_column`";
        assertEquals(expectedSql, sql);
    }

    @DisplayName("add column command to sql 테스트")
    @Test
    void testAddColumnCommandToSql() {
        // given
        Column column1 = Column.builder()
                .name("new_column")
                .type("varchar(255)")
                .isNull(true)
                .defaultValue("asdf")
                .isUnique(true)
                .isAutoIncrement(false)
                .comment("new column comment")
                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        Column column2 = Column.builder()
                .name("new_column")
                .type("varchar(255)")
                .isNull(false)
                .defaultValue("")
                .isUnique(false)
                .isAutoIncrement(false)
                .comment("new column comment")
                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        DDLRequestDTO dto1 = new AddColumnRequestDTO("test_schema", "test_table", column1);
        dto1.setCommandType(CommandType.ADD_COLUMN);
        DDLRequestDTO dto2 = new AddColumnRequestDTO("test_schema", "test_table", column2);
        dto2.setCommandType(CommandType.ADD_COLUMN);
        // when
        String sql1 = ddlParser.commandToSql(dto1);
        String sql2 = ddlParser.commandToSql(dto2);
        // then
        String expectedSql1 = "ALTER TABLE `test_schema`.`test_table` ADD COLUMN `new_column` varchar(255) DEFAULT 'asdf' UNIQUE COMMENT 'new column comment'";
        String expectedSql2 = "ALTER TABLE `test_schema`.`test_table` ADD COLUMN `new_column` varchar(255) NOT NULL COMMENT 'new column comment'";
        assertEquals(expectedSql1, sql1);
        assertEquals(expectedSql2, sql2);
    }

}