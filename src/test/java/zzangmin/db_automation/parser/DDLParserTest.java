package zzangmin.db_automation.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zzangmin.db_automation.dto.request.ddl.*;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DDLParserTest {

    @DisplayName("rename column command to sql 테스트")
    @Test
    void testRenameColumnCommandToSql() {
        // given
        DDLRequestDTO dto = new RenameColumnRequestDTO("test_schema", "test_table", "old_column", "new_column");
        // when
        String sql = dto.toSQL();

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
//                .isUnique(true)
                .isAutoIncrement(false)
                .comment("new column comment")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        Column column2 = Column.builder()
                .name("new_column")
                .type("varchar(255)")
                .isNull(false)
                .defaultValue("")
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("new column comment")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        DDLRequestDTO dto1 = new AddColumnRequestDTO("test_schema", "test_table", column1);
        DDLRequestDTO dto2 = new AddColumnRequestDTO("test_schema", "test_table", column2);
        // when
        String sql1 = dto1.toSQL();
        String sql2 = dto2.toSQL();
        // then
        String expectedSql1 = "ALTER TABLE `test_schema`.`test_table` ADD COLUMN `new_column` varchar(255) DEFAULT 'asdf' COMMENT 'new column comment'";
        String expectedSql2 = "ALTER TABLE `test_schema`.`test_table` ADD COLUMN `new_column` varchar(255) NOT NULL DEFAULT '' COMMENT 'new column comment'";
        assertEquals(expectedSql1, sql1);
        assertEquals(expectedSql2, sql2);
    }


    @DisplayName("create index command to sql 테스트")
    @Test
    void testCreateIndexCommandToSql() {
        // given
        DDLRequestDTO dto = new CreateIndexRequestDTO("test_schema", "test_table", "test_index", "KEY", List.of("test","index"));
        // when
        String sql = dto.toSQL();
        // then
        String expectedSql = "ALTER TABLE `test_schema`.`test_table` ADD INDEX `test_index` (`test`,`index`)";
        assertEquals(expectedSql, sql);
    }

    @Test
    void testCreateTableCommandToSql() {
        // given
        Column column1 = Column.builder()
                .name("test_column")
                .type("varchar(255)")
                .isNull(true)
                .defaultValue("asdf")
//                .isUnique(true)
                .isAutoIncrement(false)
                .comment("new column comment")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        Column column2 = Column.builder()
                .name("test_column_two")
                .type("varchar(255)")
                .isNull(false)
                .defaultValue("")
//                .isUnique(false)
                .isAutoIncrement(false)
                .comment("new column comment")
//                .charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        Constraint constraint1 = Constraint.builder()
                .constraintType(Constraint.ConstraintType.PRIMARY)
                .keyName("test_column")
                .keyColumnNames(List.of("test_column"))
                .build();
        Constraint constraint2 = Constraint.builder()
                .constraintType(Constraint.ConstraintType.KEY)
                .keyName("test_table")
                .keyColumnNames(List.of("test_column_two"))
                .build();

        DDLRequestDTO dto = new CreateTableRequestDTO("test_schema", "test_table", new LinkedHashSet<>(Set.of(column1, column2)), new LinkedHashSet<>(Set.of(constraint1, constraint2)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "test table comment");

        // when
        String sql = dto.toSQL();
        // then
        System.out.println("sql = " + sql);
        assertTrue(sql.contains("CREATE TABLE `test_schema`.`test_table` ("));
        assertTrue(sql.contains("`test_column` varchar(255) DEFAULT 'asdf' COMMENT 'new column comment'"));
        assertTrue(sql.contains("PRIMARY KEY (`test_column`)"));
        assertTrue(sql.contains("KEY `test_table` (`test_column_two`)"));
        assertTrue(sql.contains(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='test table comment'"));
    }






}
