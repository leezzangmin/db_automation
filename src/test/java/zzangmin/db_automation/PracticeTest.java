package zzangmin.db_automation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.tableschemacheck.TableDifferenceChecker;

import java.util.List;

@SpringBootTest
public class PracticeTest {

    @Autowired
    private MysqlClient mysqlClient;
    @Autowired
    private TableDifferenceChecker tableDifferenceChecker;

    @Test
    public void tableDifferenceCheckerTest() {
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://prod.cluster-cpx3y0rpnqaj.ap-northeast-2.rds.amazonaws.com", "admin", "mysql5128*", null);
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("inhouse", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://inhouse.cpx3y0rpnqaj.ap-northeast-2.rds.amazonaws.com", "admin", "mysql5128*", null);
        String s = tableDifferenceChecker.compareTableSchema(prod, stage, List.of("sample_schema"));
        System.out.println("s = " + s);
    }

    @Test
    public void test() {
        String asdf = "'asdf'@'10.100.0.0/255.255.0.0'";
        String[] split = asdf.split("@");
        System.out.println("split = " + split);
        for (String s : split) {
            System.out.println("s = " + s);
        }
    }

}
