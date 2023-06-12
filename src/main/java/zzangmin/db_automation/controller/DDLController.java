package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.dto.response.CreateIndexResponseDTO;
import zzangmin.db_automation.dto.response.CreateTableResponseDTO;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DDLService;
import zzangmin.db_automation.validator.DDLValidator;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DDLController {

    private final DDLService ddlService;
    private final DDLValidator ddlValidator;

    // TODO: 인증/인가

    @PostMapping("/ddl/validate")
    public String validCommand(@RequestParam String dbName, @RequestParam String ddlCommand) {
        return "ok";

    }

    @PutMapping("/ddl/column")
    public String addColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody AddColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PatchMapping("/ddl/column")
    public String alterColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody AlterColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PutMapping("/ddl/index")
    public CreateIndexResponseDTO createIndex(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestBody CreateIndexRequestDTO ddlRequestDTO) {
        ddlValidator.validateCreateIndex(databaseConnectionInfo, ddlRequestDTO);
        return ddlService.createIndex(databaseConnectionInfo, ddlRequestDTO);
    }

    @PutMapping("/ddl/table")
    public CreateTableResponseDTO createTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestBody CreateTableRequestDTO ddlRequestDTO) {
        ddlValidator.validateCreateTable(databaseConnectionInfo, ddlRequestDTO);
        return ddlService.createTable(databaseConnectionInfo, ddlRequestDTO);
    }

    @DeleteMapping("/ddl/column")
    public String deleteColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody DeleteColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PatchMapping("/ddl/varchar")
    public String extendVarcharColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody ExtendVarcharColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

}
