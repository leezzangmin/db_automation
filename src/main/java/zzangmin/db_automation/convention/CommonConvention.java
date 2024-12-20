package zzangmin.db_automation.convention;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CommonConvention {

    public final static int TABLE_BYTE_SIZE_THRESHOLD = 123456789;
    public final static int TABLE_ROW_SIZE_THRESHOLD = 9999999;
    public static final String ENGINE_TYPE = "InnoDB";
    public static final String CHARSET = "utf8mb4";
    public static final String COLLATE = "utf8mb4_general_ci";
    public static final int BYTE_PER_CHARACTER = 4;
    public static final Set<String> ALLOWED_CONSTRAINT_TYPE = Set.of("PRIMARY KEY", "UNIQUE KEY", "KEY");

    public static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z]+(_[a-z]+)*$");
    public static final Pattern PURE_LOWER_CASE_PATTER = Pattern.compile("^[a-z_]+$");

    // https://dev.mysql.com/doc/refman/8.0/en/keywords.html
    public static final List<String> MYSQL_RESERVED_WORDS = List.of(
            "ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE",
            "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN",
            "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE",
            "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE",
            "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE", "CONVERT",
            "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT_DATE",
            "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
            "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE",
            "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED",
            "DELETE", "DENSE_RANK", "DESC", "DESCRIBE", "DETERMINISTIC",
            "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL",
            "EACH", "ELSE", "ELSEIF", "EMPTY", "ENCLOSED", "ESCAPED",
            "EXCEPT", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH",
            "FIRST_VALUE", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE",
            "FOREIGN", "FROM", "FULLTEXT", "FUNCTION", "GENERATED", "GET",
            "GRANT", "GROUP", "GROUPING", "GROUPS", "HAVING", "HIGH_PRIORITY",
            "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF",
            "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT",
            "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3",
            "INT4", "INT8", "INTEGER", "INTERSECT", "INTERVAL", "INTO",
            "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN",
            "JSON_TABLE", "KEY", "KEYS", "KILL", "LAG", "LAST_VALUE",
            "LATERAL", "LEAD", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT",
            "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK",
            "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND",
            "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB",
            "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND",
            "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG",
            "NTH_VALUE", "NTILE", "NULL", "NUMERIC", "OF", "ON", "OPTIMIZE",
            "OPTIMIZER_COSTS", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER",
            "OUTFILE", "OVER", "PARTITION", "PERCENT_RANK", "PRECISION", "PRIMARY",
            "PROCEDURE", "PURGE", "RANGE", "RANK", "READ", "READS", "READ_WRITE", "REAL",
            "RECURSIVE", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE",
            "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE",
            "ROW", "ROWS", "ROW_NUMBER", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND",
            "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT",
            "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
            "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL",
            "STARTING", "STORED", "STRAIGHT_JOIN", "SYSTEM", "TABLE", "TERMINATED",
            "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER",
            "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE",
            "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES",
            "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "VIRTUAL", "WHEN",
            "WHERE", "WHILE", "WINDOW", "WITH", "WRITE", "XOR", "YEAR_MONTH",
            "ZEROFILL", "The", "CUME_DIST", "DENSE_RANK", "EMPTY", "EXCEPT",
            "FIRST_VALUE", "GROUPING", "GROUPS", "INTERSECT", "JSON_TABLE",
            "LAG", "LAST_VALUE", "LATERAL", "LEAD", "NTH_VALUE", "NTILE",
            "OF", "OVER", "PERCENT_RANK", "RANK", "RECURSIVE", "ROW_NUMBER", "SYSTEM", "WINDOW"
    );

    public static List<String> validateSnakeCase(String str) {
        List<String> errors = new ArrayList<>();
        if (!SNAKE_CASE_PATTERN.matcher(str).matches()) {
            errors.add("snake_case 가 아닙니다: " + str);
        }
        return errors;
    }

    public static List<String> validateLowerCaseString(String str) {
        List<String> errors = new ArrayList<>();
        if (!PURE_LOWER_CASE_PATTER.matcher(str).matches()) {
            errors.add("소문자가 아닙니다: " + str);
        }
        return errors;
    }

    public static List<String> validateBlankStr(String str) {
        List<String> errors = new ArrayList<>();
        if (str == null || str.isEmpty() || str.equals(" ") || str.isBlank()) {
            errors.add("문자열이 null 입니다.");
        }
        return errors;
    }

    public static List<String> validateReservedWord(String str) {
        List<String> errors = new ArrayList<>();
        if (MYSQL_RESERVED_WORDS.contains(str)) {
            errors.add("MySQL 예약어입니다 : " + str);
        }
        return errors;
    }

}
