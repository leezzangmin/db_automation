package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.standardcheck.StandardCheckResultResponseDTO;
import zzangmin.db_automation.entity.MonitorTargetDatabase;
import zzangmin.db_automation.entity.mysqlobject.MysqlAccount;
import zzangmin.db_automation.standardvalue.AccountStandard;
import zzangmin.db_automation.service.AwsService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountStandardChecker {

    private final MysqlClient mysqlClient;
    private final AwsService awsService;

    public List<StandardCheckResultResponseDTO> checkAccountStandard() {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();

        List<DatabaseConnectionInfo> databaseConnectionInfos = DynamicDataSourceProperties.findAllDatabases()
                .values()
                .stream()
                .toList();
        for (DatabaseConnectionInfo databaseConnectionInfo : databaseConnectionInfos) {
            log.info("databaseConnectionInfo: {}", databaseConnectionInfo);
            String masterUsername;
            if (databaseConnectionInfo.getDatabaseType().equals(MonitorTargetDatabase.DatabaseType.CLUSTER)
                    || databaseConnectionInfo.getDatabaseType().equals(MonitorTargetDatabase.DatabaseType.INSTANCE)) {
                masterUsername = awsService.findClusterMasterUserName(databaseConnectionInfo);
            } else {
                masterUsername = null;
            }
            List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(databaseConnectionInfo)
                    .stream()
                    .filter(account -> !AccountStandard.getAccountBlackList()
                            .contains(account.getUser()))
                    .filter((account -> !account.getUser().equals(masterUsername)))
                    .collect(Collectors.toList());
            for (MysqlAccount mysqlAccount : mysqlAccounts) {
                results.addAll(checkPrivilege(mysqlAccount, databaseConnectionInfo));

                if (AccountStandard.isAccountHostPercentAllow()) {
                    results.addAll(checkHost(mysqlAccount, databaseConnectionInfo));
                }
            }
            if (AccountStandard.isMasterUserEnable()) {
                results.addAll(checkMasterUserExists(masterUsername, mysqlAccounts, databaseConnectionInfo));
            }
        }
        log.info("account standard check finish: {}", results);
        return results;
    }

    private List<StandardCheckResultResponseDTO> checkPrivilege(MysqlAccount mysqlAccount, DatabaseConnectionInfo databaseConnectionInfo) {
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();
        List<MysqlAccount.Privilege> privileges = mysqlAccount.getPrivileges();

        for (MysqlAccount.Privilege privilege : privileges) {
            if (!AccountStandard.getApplicationAccountAllowedPrivileges().contains(privilege.getPermissionType())) {
                results.add(new StandardCheckResultResponseDTO(
                        databaseConnectionInfo.getAccountId(),
                        databaseConnectionInfo.getDatabaseName(),
                        StandardCheckResultResponseDTO.StandardType.ACCOUNT,
                        null,
                        null,
                        null,
                        String.format("`%s` 계정에 허용되지 않은 `%s` TO `%s`.`%s` 권한이 존재합니다.",
                                mysqlAccount.getUser(),
                                privilege.getPermissionType(),
                                privilege.getDatabaseName(),
                                privilege.getObjectName())
                ));
            }
        }
        return results;
    }


    private List<StandardCheckResultResponseDTO> checkHost(MysqlAccount mysqlAccount, DatabaseConnectionInfo databaseConnectionInfo) {
        List<StandardCheckResultResponseDTO> resultList = new ArrayList<>();
        if ("%".equals(mysqlAccount.getHost()) || "'%".equals(mysqlAccount.getHost())) {
            resultList.add(new StandardCheckResultResponseDTO(
                    databaseConnectionInfo.getAccountId(),
                    databaseConnectionInfo.getDatabaseName(),
                    StandardCheckResultResponseDTO.StandardType.ACCOUNT,
                    null,
                    null,
                    null,
                    String.format("`%s` 계정의 host가 %%로 설정되어 있습니다.", mysqlAccount.getUser())
            ));
        }
        return resultList;
    }


    private List<StandardCheckResultResponseDTO> checkMasterUserExists(String masterUsername, List<MysqlAccount> mysqlAccounts, DatabaseConnectionInfo databaseConnectionInfo) {
        if (masterUsername == null || masterUsername.isEmpty()) {
            return new ArrayList<>();
        }

        List<StandardCheckResultResponseDTO> resultList = new ArrayList<>();
        List<String> users = mysqlAccounts.stream().map(MysqlAccount::getUser).toList();

        if (users.contains(masterUsername)) {
            resultList.add(new StandardCheckResultResponseDTO(
                    databaseConnectionInfo.getAccountId(),
                    databaseConnectionInfo.getDatabaseName(),
                    StandardCheckResultResponseDTO.StandardType.ACCOUNT,
                    null,
                    null,
                    null,
                    "masterUser 가 활성화되어있습니다. masterUsername: " + masterUsername
            ));
        }
        return resultList;
    }
}
