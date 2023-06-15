package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.dto.response.*;
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

    @GetMapping("/ddl/validate")
    public String validCommand(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                               @RequestBody DDLRequestDTO ddlRequestDTO) {
        ddlValidator.validateDDLRequest(databaseConnectionInfo, ddlRequestDTO);
        return "ok";
    }

    @PutMapping("/ddl/column")
    public AddColumnResponseDTO addColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                            @RequestBody AddColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateAddColumn(databaseConnectionInfo, ddlRequestDTO);
        return ddlService.addColumn(databaseConnectionInfo, ddlRequestDTO);
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
                                              @RequestBody CreateTableRequestDTO ddlRequestDTO) throws InterruptedException {
        ddlValidator.validateCreateTable(databaseConnectionInfo, ddlRequestDTO);
        return ddlService.createTable(databaseConnectionInfo, ddlRequestDTO);
    }

    // TODO: rename -> delete
    @DeleteMapping("/ddl/column")
    public DeleteColumnResponseDTO deleteColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                @RequestBody DeleteColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateDeleteColumn(databaseConnectionInfo, ddlRequestDTO);
        return ddlService.deleteColumn(databaseConnectionInfo, ddlRequestDTO);
    }

    @PatchMapping("/ddl/varchar")
    public ExtendVarcharColumnResponseDTO extendVarcharColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                              @RequestBody ExtendVarcharColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateExtendVarchar(databaseConnectionInfo, ddlRequestDTO);
        return ddlService.extendVarcharColumn(databaseConnectionInfo, ddlRequestDTO);
    }

}
