package zzangmin.db_automation.convention;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zzangmin.db_automation.entity.mysqlobject.Column;
import zzangmin.db_automation.entity.mysqlobject.Constraint;
import zzangmin.db_automation.entity.mysqlobject.Table;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


class TableConventionTest {

    @DisplayName("table convention validation이 정상적으로 수행되어야 한다.")
    @Test
    void validateTableConventionTest() {
        // given
        Column column1 = new Column("id", "BIGINT", false, null, true, "id column comment", "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name", "VARCHAR(255)", false, null, false, "name column comment", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.KEY, "name", List.of("name"));
        Constraint constraint3 = new Constraint(Constraint.ConstraintType.KEY, "id_name", List.of("id_name"));
        Table table = new Table("table_name", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<> (Set.of(constraint1, constraint2, constraint3)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        //when & then
        Assertions.assertDoesNotThrow(() -> TableConvention.validateTableConvention(table));

    }

    @DisplayName("snake_case가 아닌 table 이름의 convention validation은 실패해야 한다.")
    @Test
    void validateTableConventionTest_noSnakeCase() {
        // given
        Column column1 = new Column("id", "BIGINT", false, null,  true, "id column comment", "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name", "VARCHAR(255)", false, null, false, "name column comment", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.KEY, "name", List.of("name"));
        Constraint constraint3 = new Constraint(Constraint.ConstraintType.KEY, "id_name", List.of("id_name"));
        Table table = new Table("NOTSNAKE123", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<> (Set.of(constraint1, constraint2, constraint3)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table));

    }

    @DisplayName("snake_case가 아닌 컬럼 이름의 convention validation은 실패해야 한다.")
    @Test
    void validateTableConventionTest_noSnakeCaseColumnName() {
        // given
        Column column1 = new Column("id", "BIGINT", false, null, true, "id column comment", "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name123", "VARCHAR(255)", false, null, false, "name column comment", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.KEY, "name", List.of("name"));
        Constraint constraint3 = new Constraint(Constraint.ConstraintType.KEY, "id_name", List.of("id_name"));
        Table table = new Table("test_table", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<> (Set.of(constraint1, constraint2, constraint3)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table));

    }

    @DisplayName("snake_case가 아닌 인덱스 이름의 convention validation은 실패해야 한다.")
    @Test
    void validateTableConventionTest_noSnakeCaseIndexName() {
        // given
        Column column1 = new Column("id", "BIGINT", false, null, true, "id column comment",  "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name", "VARCHAR(255)", false, null, false, "name column comment", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.KEY, "a1", List.of("name"));
        Constraint constraint3 = new Constraint(Constraint.ConstraintType.KEY, "id_name", List.of("id_name"));
        Table table = new Table("test_table", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<> (Set.of(constraint1, constraint2, constraint3)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table));

    }

    @DisplayName("중복된 컬럼명을 가진 table의 convention validation은 실패해야 한다.")
    @Test
    void validateTableConventionTest_duplicateColumnName() {
        // given
        Column column1 = new Column("id", "BIGINT", false, null, true, "id column comment",  "utf8mb4_0900_ai_ci");
        Column column2 = new Column("id", "VARCHAR(255)", false, null, false, "name column comment", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Table table = new Table("NOTSNAKE123", new LinkedHashSet<>(Set.of(column1, column2)), new LinkedHashSet<>(Set.of(constraint1)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table));

    }

    @DisplayName("컨벤션과 어긋난 옵션을 가진 table의 validation은 실패해야 한다.")
    @Test
    void validateTableConventionTest_invalidTableOptions() {
        // given
        Column column1 = new Column("id", "BIGINT", false, null, true, "id column comment", "utf8mb4_0900_ai_ci");
        Column column2 = new Column("name", "VARCHAR(255)", false, null, false, "name column comment", "utf8mb4_0900_ai_ci");
        Constraint constraint1 = new Constraint(Constraint.ConstraintType.PRIMARY, "id", List.of("id"));
        Constraint constraint2 = new Constraint(Constraint.ConstraintType.KEY, "name", List.of("name"));
        Constraint constraint3 = new Constraint(Constraint.ConstraintType.KEY, "id_name", List.of("id_name"));
        Table table1 = new Table("table_name", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<>(Set.of(constraint1, constraint2, constraint3)), "NOENGINE", "utf8mb4", "utf8mb4_0900_ai_ci", "table comment");
        Table table2 = new Table("table_name", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<>(Set.of(constraint1, constraint2, constraint3)), "InnoDB", "invalidaCharacterSet", "utf8mb4_0900_ai_ci", "table comment");
        Table table3 = new Table("table_name", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<>(Set.of(constraint1, constraint2, constraint3)), "InnoDB", "utf8mb4", "invalidCollate", "table comment");
        Table table4 = new Table("table_name", new LinkedHashSet<> (Set.of(column1, column2)), new LinkedHashSet<>(Set.of(constraint1, constraint2, constraint3)), "InnoDB", "utf8mb4", "utf8mb4_0900_ai_ci", null);
        //when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table3));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableConvention.validateTableConvention(table4));

    }
}