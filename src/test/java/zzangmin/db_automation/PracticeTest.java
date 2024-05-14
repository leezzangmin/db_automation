package zzangmin.db_automation;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.util.deparser.GrantDeParser;
import net.sf.jsqlparser.util.validation.validator.GrantValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.schedule.mysqlobjectcheck.TableDifferenceChecker;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class PracticeTest {

    @Test
    public void test() throws Exception {
        String createTableSQL = "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER ON `upbitcare`.* TO `upbitcare_user`@`10.191.0.0/255.255.0.0`;\n";
        //GrantValidator grantValidator = new GrantValidator();
        //CCJSqlParserManager ccjSqlParserManager = new CCJSqlParserManager();
        //Grant grant = (Grant) ccjSqlParserManager.parse(new StringReader(createTableSQL));
        Statement statement = CCJSqlParserUtil.parse(createTableSQL);
        System.out.println("statement = " + statement);


    }

}
