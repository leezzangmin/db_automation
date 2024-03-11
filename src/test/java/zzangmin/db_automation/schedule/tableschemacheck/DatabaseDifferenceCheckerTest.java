package zzangmin.db_automation.schedule.tableschemacheck;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseDifferenceCheckerTest {

    @Autowired
    private DatabaseDifferenceChecker databaseDifferenceChecker;

    @DisplayName("compareDatabase로 database 생성 구문")
    @Test
    void testCompareDatabase() {
        // given
        DatabaseConnectionInfo prod = new DatabaseConnectionInfo("prod", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://prod.cluster-cpx3y0rpnqaj.ap-northeast-2.rds.amazonaws.com", "admin", "mysql5128*");
        DatabaseConnectionInfo stage = new DatabaseConnectionInfo("inhouse", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://inhouse.cpx3y0rpnqaj.ap-northeast-2.rds.amazonaws.com", "admin", "mysql5128*");

        // when
        databaseDifferenceChecker.compareDatabase(prod, stage);

        //then
        throw new RuntimeException();
    }
}