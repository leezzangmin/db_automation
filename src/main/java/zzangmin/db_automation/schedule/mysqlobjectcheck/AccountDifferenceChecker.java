package zzangmin.db_automation.schedule.mysqlobjectcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.MysqlAccount;
import zzangmin.db_automation.service.MysqlAccountService;
import zzangmin.db_automation.util.ProfileUtil;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountDifferenceChecker {
    private final MysqlClient mysqlClient;
    private final MysqlAccountService mysqlAccountService;

    public String compareAccount(DatabaseConnectionInfo sourceInfo, DatabaseConnectionInfo replicaInfo) {
        StringBuilder differenceResult = new StringBuilder();
//        List<MysqlAccount> mysqlAccounts1 = mysqlClient.findMysqlAccounts(sourceInfo);
//        List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(replicaInfo);
//        for (MysqlAccount mysqlAccount : mysqlAccounts) {
//            log.info("mysqlaccount: {}", mysqlAccount);
//        }
//        log.info("AccountDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public String compareAccountCrossAccount(DatabaseConnectionInfo databaseConnectionInfo) {
        StringBuilder differenceResult = new StringBuilder();
        List<MysqlAccount> sourceMysqlAccounts = mysqlClient.findMysqlAccounts(databaseConnectionInfo);
        List<MysqlAccount> replicaMysqlAccounts = mysqlAccountService.findAccounts(databaseConnectionInfo.findServiceName());
        for (MysqlAccount sourceMysqlAccount : sourceMysqlAccounts) {
            if (!isAccountExists(sourceMysqlAccount, replicaMysqlAccounts)) {
                differenceResult.append(String.format("`%s` 환경, `%s` DB에 `%s` 계정이 존재하지 않습니다.\n",
                        ProfileUtil.CURRENT_ENVIRONMENT_PROFILE,
                        databaseConnectionInfo.findServiceName(),
                        sourceMysqlAccount.getUser()));
                continue;
            }
            List<MysqlAccount.Privilege> sourcePrivileges = sourceMysqlAccount.getPrivileges();
            List<MysqlAccount.Privilege> replicaPrivileges = findAccount(sourceMysqlAccount, replicaMysqlAccounts).getPrivileges();
            if (sourcePrivileges.size() != replicaPrivileges.size()) {
                differenceResult.append(String.format("`%s` 환경, `%s` DB에 `%s` 계정 권한 개수가 다릅니다. `%s` <-> `%s`\n",
                        ProfileUtil.CURRENT_ENVIRONMENT_PROFILE,
                        databaseConnectionInfo.findServiceName(),
                        sourceMysqlAccount.getUser(),
                        sourcePrivileges.size(),
                        replicaPrivileges.size()));
            }
            for (MysqlAccount.Privilege sourcePrivilege : sourcePrivileges) {
                if (!isPrivilegeExists(sourcePrivilege, replicaPrivileges)) {
                    differenceResult.append(String.format("`%s` 환경, `%s` DB, `%s` 계정에 `%s` TO %s.%s 권한이 존재하지 않습니다.\n",
                            ProfileUtil.CURRENT_ENVIRONMENT_PROFILE,
                            databaseConnectionInfo.findServiceName(),
                            sourceMysqlAccount.getUser(),
                            sourcePrivilege.getPermissionType(),
                            sourcePrivilege.getDatabaseName(),
                            sourcePrivilege.getObjectName()));
                }
            }
        }
        log.info("AccountDifferenceChecker Result: {}", differenceResult.toString());
        return differenceResult.toString();
    }

    public void saveAccount(DatabaseConnectionInfo databaseConnectionInfo) {
        log.info("saveAccount: {}", databaseConnectionInfo);
        List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(databaseConnectionInfo);
        mysqlAccountService.upsertAccounts(mysqlAccounts);
    }

    private boolean isPrivilegeExists(MysqlAccount.Privilege sourcePrivilege, List<MysqlAccount.Privilege> replicaPrivileges) {
        for (MysqlAccount.Privilege replicaPrivilege : replicaPrivileges) {
            if(replicaPrivilege.getDatabaseName().equals(sourcePrivilege.getDatabaseName()) &&
            replicaPrivilege.getObjectName().equals(sourcePrivilege.getObjectName()) &&
            replicaPrivilege.getPermissionType().equals(sourcePrivilege.getPermissionType())) {
                return true;
            }
        }
        return false;
    }
    private boolean isAccountExists(MysqlAccount mysqlAccount, List<MysqlAccount> targetMysqlAccounts) {
        for (MysqlAccount targetMysqlAccount : targetMysqlAccounts) {
            if (targetMysqlAccount.getUser().equals(mysqlAccount.getUser()) &&
                    targetMysqlAccount.getServiceName().equals(mysqlAccount.getServiceName())) {
                return true;
            }
        }
        return false;
    }

    private MysqlAccount findAccount(MysqlAccount mysqlAccount, List<MysqlAccount> targetMysqlAccounts) {
        for (MysqlAccount targetMysqlAccount : targetMysqlAccounts) {
            if (targetMysqlAccount.getUser().equals(mysqlAccount.getUser()) &&
                    targetMysqlAccount.getServiceName().equals(mysqlAccount.getServiceName())) {
                return targetMysqlAccount;
            }
        }
        log.info("mysqlAccount: {}", mysqlAccount);
        throw new IllegalStateException("일치하는 계정이 존재하지 않습니다.");
    }
}
