package zzangmin.db_automation;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PracticeTest {

    @Test
    public void test() throws Exception {
        List<String> MYSQL_RESERVED_WORDS = List.of(
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
        System.out.println("MYSQL_RESERVED_WORDS.size() = " + MYSQL_RESERVED_WORDS.size());
    }

    @Test
    public void test2() throws Exception {
        String selectSQL = "SELECT * FROM `upbitcare` as a inner join asdf as " +
                "BB on a.id=BB.id and a.id=1" +
                " WHERE `a.id` = 1 or BB.dfe like '%asdf%' group by a.id order by bb.id,a.name offset 1 limit 1,1;";
        Select select = (Select) CCJSqlParserUtil.parse(selectSQL);
        System.out.println("select = " + select);
        Offset offset = select.getOffset();
        System.out.println("offset = " + offset);
        Limit limit = select.getLimit();
        System.out.println("limit = " + limit);
        Fetch fetch = select.getFetch();
        System.out.println("fetch = " + fetch);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
            @Override
            public void visit(PlainSelect plainSelect) {
                System.out.println("plainselect -> " + plainSelect.toString());
                for (SelectItem item : plainSelect.getSelectItems()) {
                    System.out.println(item.toString());
                }
                super.visit(plainSelect);
            }
        };
        List<String> tableList = tablesNamesFinder.getTableList((Statement) select);
        System.out.println("tableList = " + tableList);

        Expression where = plainSelect.getWhere();
        System.out.println("where = " + where);
        Expression having = plainSelect.getHaving();
        System.out.println("having = " + having);
        List<Join> joins = plainSelect.getJoins();
        for (Join join : joins) {
            System.out.println("join = " + join);
        }
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
        for (OrderByElement orderByElement : orderByElements) {
            System.out.println("orderByElement = " + orderByElement);
        }
    }

}
