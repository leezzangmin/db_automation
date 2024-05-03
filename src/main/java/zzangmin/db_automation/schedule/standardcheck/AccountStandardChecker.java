package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.MysqlAccount;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.AccountStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AccountStandardChecker {

    private final MysqlClient mysqlClient;
    private final AwsService awsService;

    public String checkAccountStandard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        List<DatabaseConnectionInfo> databaseConnectionInfos = DynamicDataSourceProperties.getDatabases()
                .values()
                .stream()
                .collect(Collectors.toList());
        for (DatabaseConnectionInfo databaseConnectionInfo : databaseConnectionInfos) {
            List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(databaseConnectionInfo)
                    .stream()
                    .filter(account -> AccountStandard.getAccountBlackList()
                            .contains(account))
                    .collect(Collectors.toList());
            for (MysqlAccount mysqlAccount : mysqlAccounts) {
                sb.append(checkPrivilege(mysqlAccount));
                if (AccountStandard.isAccountHostPercentAllow()) {
                    sb.append(checkHost(mysqlAccount));
                }
            }
            if (AccountStandard.isMasterUserEnable()) {
                sb.append(checkMasterUserExists(mysqlAccounts, databaseConnectionInfo));
            }
        }
        return sb.toString();
    }

    private String checkPrivilege(MysqlAccount mysqlAccount) {
        StringBuilder sb = new StringBuilder();
        List<MysqlAccount.Privilege> privileges = mysqlAccount.getPrivileges();
        for (MysqlAccount.Privilege privilege : privileges) {
            if (!AccountStandard.getApplicationAccountAllowedPrivileges().contains(privilege.getPermissionType())) {
                sb.append(String.format("`%s` 계정에 허용되지 않은 `%s` 권한이 존재합니다.\n", mysqlAccount.getUser(), privilege.getPermissionType()));
            }
        }
        return sb.toString();
    }

    private String checkHost(MysqlAccount mysqlAccount) {
        StringBuilder sb = new StringBuilder();
        if (mysqlAccount.getHost().equals("%") || mysqlAccount.getHost().equals("'%'")) {
            sb.append(String.format("`%s` 계정의 host가 %로 설정되어있습니다.\n", mysqlAccount.getUser()));
        }
        return sb.toString();
    }

    private String checkMasterUserExists(List<MysqlAccount> mysqlAccounts, DatabaseConnectionInfo databaseConnectionInfo) {
        StringBuilder sb = new StringBuilder();

        String masterUsername = awsService.findClusterMasterUserName(databaseConnectionInfo.getDatabaseName());
        if (mysqlAccounts.stream().map(account -> account.getUser()).collect(Collectors.toList()).contains(masterUsername)) {
            sb.append("masterUser 가 활성화되어있습니다. masterUsername: " + masterUsername + "\n");
        }
        return sb.toString();

    }
}
