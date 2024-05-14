package zzangmin.db_automation.testfactory;


import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.entity.Table;

import java.util.*;

public class EntityFactory {

    public static Column generateBasicColumn(String columnName) {
        Column column = Column.builder()
                .name(columnName)
                .type("varchar(255)")
                .isNull(true)
                .defaultValue(null)
                //.isUnique(false)
                .isAutoIncrement(false)
                .comment("column comment 123")
                //.charset("utf8mb4")
                .collate("utf8mb4_0900_ai_ci")
                .build();
        return column;
    }

    public static Constraint generateBasicConstraint() {
        Constraint constraint = Constraint.builder()
                .constraintType(Constraint.ConstraintType.KEY)
                .keyName("column_name")
                .keyColumnNames(new ArrayList<>(List.of("comlumn_name")))
                .build();
        return constraint;
    }

    public static Table generateBasicTable(String tableName) {
        Table table = Table.builder()
                .tableName(tableName)
                .columns((LinkedHashSet<Column>) (Set.of(generateBasicColumn("column_name"))))
                .constraints(((LinkedHashSet<Constraint>) Set.of(generateBasicConstraint())))
                .tableEngine("innoDB")
                .tableCharset("utf8mb4")
                .tableCollate("utf8mb4_0900_ai_ci")
                .tableComment("table comment123")
                .build();
        return table;
    }

}
