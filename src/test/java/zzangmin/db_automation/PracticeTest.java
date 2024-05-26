package zzangmin.db_automation;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

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
