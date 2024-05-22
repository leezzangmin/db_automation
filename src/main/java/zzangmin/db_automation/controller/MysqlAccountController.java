package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.SchemaNamesResponseDTO;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MysqlAccountController {

    @GetMapping("/account/privilege")
    public SchemaNamesResponseDTO findAccountPrivilege(DatabaseConnectionInfo databaseConnectionInfo, String accountName) {
        return describeService.findSchemaNames(databaseConnectionInfo);
    }
}
