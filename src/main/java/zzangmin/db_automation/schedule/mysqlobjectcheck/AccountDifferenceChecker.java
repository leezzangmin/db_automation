package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.MysqlAccount;
import zzangmin.db_automation.service.MysqlAccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountDifferenceChecker {
    private final MysqlClient mysqlClient;
    private final MysqlAccountService mysqlAccountService;

    public String compareAccount(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo) {
        StringBuilder differenceResult = new StringBuilder();
        List<MysqlAccount> mysqlAccounts1 = mysqlClient.findMysqlAccounts(sourceInfo);
        List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(replicaInfo);
        for (MysqlAccount mysqlAccount : mysqlAccounts) {
            log.info("mysqlaccount: {}", mysqlAccount);
        }
        log.info("AccountDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public String compareAccountCrossAccount(DatabaseConnectionInfo databaseConnectionInfo) {
        StringBuilder differenceResult = new StringBuilder();
        log.info("AccountDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveAccount(DatabaseConnectionInfo databaseConnectionInfo) {
        log.info("saveAccount: {}", databaseConnectionInfo);
        List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(databaseConnectionInfo);
        mysqlAccountService.upsertAccounts(mysqlAccounts);
    }
}
