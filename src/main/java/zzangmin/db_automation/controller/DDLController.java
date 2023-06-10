package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.dto.request.*;
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
    private final SlackClient slackClient;


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
    public String createIndex(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody CreateIndexRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PutMapping("/ddl/table")
    public CreateTableResponseDTO createTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestBody CreateTableRequestDTO ddlRequestDTO) {
        ddlValidator.validateCreateTable(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.createTable(databaseConnectionInfo, ddlRequestDTO);

        // TODO: 실행시간, 인증/인가, slack 메세지 send
        return new CreateTableResponseDTO("test@gmail.com", databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), createTableStatement);
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
