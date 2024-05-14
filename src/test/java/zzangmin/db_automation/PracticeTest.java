package zzangmin.db_automation;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.schedule.mysqlobjectcheck.TableDifferenceChecker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PracticeTest {

    @Test
    public void test() throws Exception {
        String createTableSQL = "create table dba.asdf_ddd(\n" +
                "id bigint primary key auto_increment) engine=InnoDB default charset = utf8mb4 collate=utf8mb4_0900_ai_ci comment 'comment123';";

        String createTableSQL2 = "CREATE TABLE `users` (\n" +
                "  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '사용자 고유번호',\n" +
                "  `user_name` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '국문 사용자 성명',\n" +
                "  `user_name_en` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '영문 사용자 성명',\n" +
                "  `nickname` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '닉네임',\n" +
                "  `email` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '이메일',\n" +
                "  `status` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '사용자 상태',\n" +
                "  `external_user_id` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '외부 사용자 고유번호',\n" +
                "  `created_by` bigint(20) NOT NULL COMMENT '등록자',\n" +
                "  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',\n" +
                "  `updated_by` bigint(20) DEFAULT NULL COMMENT '수정자',\n" +
                "  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',\n" +
                "  PRIMARY KEY (`user_id`),\n" +
                "  UNIQUE KEY `uk_external_user_id` (`external_user_id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=201534 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자'";

        createTableSQL = createTableSQL.replaceAll("`", "\"");
        CreateTable parse = (CreateTable) CCJSqlParserUtil.parse(createTableSQL);

        Set<Constraint> constraints = new HashSet<>();
        List<Index> indexes = parse.getIndexes();
        if (indexes != null) {
            for (Index index : indexes) {
                constraints.add(Constraint.of(index));
            }
        }

        List<ColumnDefinition> columnDefinitions = parse.getColumnDefinitions();
        Set<Column> columns = new HashSet<>();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columns.add(Column.of(columnDefinition));
        }

        List<String> tableOptionsStrings = parse.getTableOptionsStrings();
        if (tableOptionsStrings == null) {
            throw new IllegalArgumentException("table option 이 null 입니다. ex) engine, charset, collate, comment 등 ");
        }
        int engineOptionIndex = -1;
        if (tableOptionsStrings.indexOf("engine") != -1) {
            engineOptionIndex = tableOptionsStrings.indexOf("engine");
        } else if (tableOptionsStrings.indexOf("ENGINE") != -1) {
            engineOptionIndex = tableOptionsStrings.indexOf("ENGINE");
        }
        int charsetOptionIndex = -1;
        if (tableOptionsStrings.indexOf("charset") != -1) {
            charsetOptionIndex = tableOptionsStrings.indexOf("charset");
        } else if (tableOptionsStrings.indexOf("CHARSET") != -1) {
            charsetOptionIndex = tableOptionsStrings.indexOf("CHARSET");
        }
        int collateOptionIndex = -1;
        if (tableOptionsStrings.indexOf("collate") != -1) {
            collateOptionIndex = tableOptionsStrings.indexOf("collate");
        } else if (tableOptionsStrings.indexOf("COLLATE") != -1) {
            collateOptionIndex = tableOptionsStrings.indexOf("COLLATE");
        }
        int tableCommentOptionIndex = -1;
        if (tableOptionsStrings.indexOf("comment") != -1) {
            tableCommentOptionIndex = tableOptionsStrings.indexOf("comment");
        } else if (tableOptionsStrings.indexOf("COMMENT") != -1) {
            tableCommentOptionIndex = tableOptionsStrings.indexOf("COMMENT");
        }
        String tableComment;
        try {
            tableComment = tableOptionsStrings.get(tableCommentOptionIndex + 2);
        } catch (Exception e) {
            tableComment = tableOptionsStrings.get(tableCommentOptionIndex + 1);
        }
        CreateTableRequestDTO createTableRequestDTO = new CreateTableRequestDTO(parse.getTable().getSchemaName(),
                parse.getTable().getName(),
                columns,
                constraints,
                engineOptionIndex == -1 ? null : tableOptionsStrings.get(engineOptionIndex + 2),
                charsetOptionIndex == -1 ? null : tableOptionsStrings.get(charsetOptionIndex + 2),
                collateOptionIndex == -1 ? null : tableOptionsStrings.get(collateOptionIndex + 2),
                tableComment
                );
        System.out.println("createTableRequestDTO = " + createTableRequestDTO);
    }

}
