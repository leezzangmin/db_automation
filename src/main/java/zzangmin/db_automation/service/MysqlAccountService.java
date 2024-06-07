package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeGrantRequestDTO;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeShowRequestDTO;
import zzangmin.db_automation.entity.MysqlAccount;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MysqlAccountService {

    private final MysqlClient mysqlClient;

    public List<String> findAccountNames(DatabaseConnectionInfo databaseConnectionInfo) {
        List<MysqlAccount> mysqlAccounts = mysqlClient.findMysqlAccounts(databaseConnectionInfo);
        return mysqlAccounts.stream()
                .map(account -> account.getAccountString())
                .collect(Collectors.toList());
    }

    public List<String> findPrivileges(DatabaseConnectionInfo databaseConnectionInfo,
                                       MysqlPrivilegeShowRequestDTO requestDTO) {
        return mysqlClient.findPrivilegeString(databaseConnectionInfo, requestDTO.getAccountName());
    }

    public List<String> grantPrivilege(DatabaseConnectionInfo databaseConnectionInfo,
                                        MysqlPrivilegeGrantRequestDTO requestDTO) {
        mysqlClient.executeSQL(databaseConnectionInfo, requestDTO.toSQL());
        return mysqlClient.findPrivilegeString(databaseConnectionInfo, requestDTO.getAccountName());
    }
}
