package zzangmin.db_automation.parser;

import zzangmin.db_automation.entity.mysqlobject.Column;
import zzangmin.db_automation.entity.mysqlobject.Constraint;

import java.util.Set;

public class DDLParser {

    public static String generateCreateTableStatement(String schemaName, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `");
        sb.append(schemaName);
        sb.append("`.`");
        sb.append(tableName);
        sb.append("` (\n");
        return sb.toString();
    }

    public static String generateColumnStatement(Set<Column> columns) {
        StringBuilder sb = new StringBuilder();
        for (Column column : columns) {
            sb.append("`");
            sb.append(column.getName());
            sb.append("` ");
            sb.append(column.getType());
            sb.append(" ");
            sb.append(column.generateNull());
            sb.append(column.generateAutoIncrement());
            sb.append(" COMMENT '");
            sb.append(column.getComment());
            sb.append("',\n");
        }
        if (sb.isEmpty()) {
            return sb.toString();
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }


    public static String generateConstraintStatement(Set<Constraint> constraints) {
        StringBuilder sb = new StringBuilder();
        for (Constraint constraint : constraints) {
            sb.append(constraint.getConstraintType().typeName);
            if (!constraint.getConstraintType().equals(Constraint.ConstraintType.PRIMARY)) {
                sb.append(" `");
                sb.append(constraint.getKeyName());
                sb.append("`");
            }
            sb.append(" (");
            for (String keyName : constraint.getKeyColumnNames()) {
                sb.append("`");
                sb.append(keyName);
                sb.append("`,");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("),\n");
        }
        if (sb.isEmpty()) {
            return sb.toString();
        }
        StringBuilder firstComma = new StringBuilder();
        firstComma.append(",");
        firstComma.append(sb);
        firstComma.deleteCharAt(firstComma.lastIndexOf(","));
        return firstComma.toString();
    }

    public static String generateCreateTableOptions(String engine, String charset, String collate, String comment) {
        StringBuilder sb = new StringBuilder();
        sb.append(") ENGINE=");
        sb.append(engine);
        sb.append(" DEFAULT CHARSET=");
        sb.append(charset);
        sb.append(" COLLATE=");
        sb.append(collate);
        sb.append(" COMMENT='");
        sb.append(comment);
        sb.append("'");
        return sb.toString();
    }
}
