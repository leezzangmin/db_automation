package zzangmin.db_automation.controller;

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.MysqlPrivilegeResponseDTO;
import zzangmin.db_automation.service.MysqlAccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MysqlAccountController {

    private final MysqlAccountService mysqlAccountService;

    @GetMapping("/account")
    public List<String> findAccountNames(DatabaseConnectionInfo databaseConnectionInfo) {
        return mysqlAccountService.findAccountNames(databaseConnectionInfo);
    }

    @GetMapping("/account/privilege")
    public MysqlPrivilegeResponseDTO findAccountPrivilege(DatabaseConnectionInfo databaseConnectionInfo,
                                                          String accountName,
                                                          ViewSubmissionPayload.User slackUser) {
        validateAccountName(accountName);
        List<String> privileges = mysqlAccountService.findPrivileges(databaseConnectionInfo, accountName);
        return new MysqlPrivilegeResponseDTO(accountName, privileges);
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
