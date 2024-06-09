package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.account.AccountRequestDTO;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeGrantRequestDTO;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeShowRequestDTO;
import zzangmin.db_automation.dto.response.account.MysqlPrivilegeGrantResponseDTO;
import zzangmin.db_automation.dto.response.account.MysqlPrivilegeResponseDTO;
import zzangmin.db_automation.service.MysqlAccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MysqlAccountController {

    private final MysqlAccountService mysqlAccountService;

    public void validateAccountRequest(DatabaseConnectionInfo databaseConnectionInfo, AccountRequestDTO requestDTO) {

    }

    @GetMapping("/account")
    public List<String> findAccountNames(DatabaseConnectionInfo databaseConnectionInfo) {
        return mysqlAccountService.findAccountNames(databaseConnectionInfo);
    }

    @GetMapping("/account/privilege")
    public MysqlPrivilegeResponseDTO findAccountPrivilege(DatabaseConnectionInfo databaseConnectionInfo,
                                                          MysqlPrivilegeShowRequestDTO requestDTO,
                                                          String slackUserId) {
        validateAccountName(requestDTO.getAccountName());
        List<String> privileges = mysqlAccountService.findPrivileges(databaseConnectionInfo, requestDTO);
        return new MysqlPrivilegeResponseDTO(requestDTO.getAccountName(), privileges);
    }

    @PostMapping("/account/privilege")
    public MysqlPrivilegeGrantResponseDTO grantAccountPrivilege(DatabaseConnectionInfo databaseConnectionInfo,
                                        MysqlPrivilegeGrantRequestDTO requestDTO,
                                        String slackUserId) {
        validateAccountName(requestDTO.getAccountName());
        List<String> privileges = mysqlAccountService.grantPrivilege(databaseConnectionInfo, requestDTO);
        return new MysqlPrivilegeGrantResponseDTO(requestDTO.getAccountName(), privileges);
    }

    private void validateAccountName(String accountName) {
        if (!accountName.startsWith("'") || !accountName.endsWith("'")) {
            throw new IllegalArgumentException("올바른 계정 형식이 아닙니다.");
        }
        String accountNameReplaced = accountName.replaceAll("'", "");
        if (!accountNameReplaced.contains("@")) {
            throw new IllegalArgumentException("올바른 계정 형식이 아닙니다.");
        }
        String[] splitAccountName = accountNameReplaced.split("@");
        String user = splitAccountName[0];
        String host = splitAccountName[1];
        // additional validation TODO
    }

}
