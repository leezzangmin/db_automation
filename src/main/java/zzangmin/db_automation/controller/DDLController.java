package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.request.ddl.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.ddl.*;
import zzangmin.db_automation.service.ChangeHistoryService;
import zzangmin.db_automation.service.DDLService;
import zzangmin.db_automation.validator.DDLValidator;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DDLController {

    private final DDLService ddlService;
    private final ChangeHistoryService changeHistoryService;
    private final DDLValidator ddlValidator;

    @GetMapping("/ddl/validate")
    public String validateCommand(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                  @RequestBody DDLRequestDTO ddlRequestDTO) {
        ddlValidator.validateDDLRequest(databaseConnectionInfo, ddlRequestDTO);
        return "ok";
    }

    @PatchMapping("/ddl/table/name")
    public RenameTableDDLResponseDTO renameTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                             @RequestBody RenameTableRequestDTO ddlRequestDTO,
                              String slackUserId) {
        ddlValidator.validateRenameTable(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.renameTable(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getOldTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new RenameTableDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getOldTableName(),
                ddlRequestDTO.getNewTableName(),
                createTableStatement);
    }

    @PutMapping("/ddl/column")
    public AddColumnDDLResponseDTO addColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                             @RequestBody AddColumnRequestDTO ddlRequestDTO,
                                             String slackUserId) {
        ddlValidator.validateAddColumn(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.addColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new AddColumnDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                ddlRequestDTO.getColumn().getName(),
                createTableStatement);
    }

    @PatchMapping("/ddl/column")
    public AlterColumnDDLResponseDTO alterColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                 @RequestBody AlterColumnRequestDTO ddlRequestDTO,
                                                 String slackUserId) {
        ddlValidator.validateAlterColumn(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.alterColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new AlterColumnDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                createTableStatement);
    }

    @PutMapping("/ddl/index")
    public CreateIndexDDLResponseDTO createIndex(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                 @RequestBody CreateIndexRequestDTO ddlRequestDTO,
                                                 String slackUserId) {
        ddlValidator.validateCreateIndex(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.createIndex(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new CreateIndexDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                ddlRequestDTO.getIndexName(),
                createTableStatement);
    }

    @PutMapping("/ddl/table")
    public CreateTableDDLResponseDTO createTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                 @RequestBody CreateTableRequestDTO ddlRequestDTO,
                                                 String slackUserId) {
        log.info("createTableRequestDTO: {}", ddlRequestDTO);
        ddlValidator.validateCreateTable(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.createTable(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new CreateTableDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                createTableStatement);
    }

    @DeleteMapping("/ddl/column")
    public DeleteColumnDDLResponseDTO deleteColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                   @RequestBody DeleteColumnRequestDTO ddlRequestDTO,
                                                   String slackUserId) {
        ddlValidator.validateDeleteColumn(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.deleteColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new DeleteColumnDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                ddlRequestDTO.getColumnName(),
                createTableStatement);
    }

    @PatchMapping("/ddl/varchar")
    public ExtendVarcharColumnDDLResponseDTO extendVarcharColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                                 @RequestBody ExtendVarcharColumnRequestDTO ddlRequestDTO,
                                                                 String slackUserId) {
        ddlValidator.validateExtendVarchar(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.extendVarcharColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new ExtendVarcharColumnDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                createTableStatement);
    }

    @PatchMapping("/ddl/column/name")
    public RenameColumnDDLResponseDTO renameColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                   @RequestBody RenameColumnRequestDTO ddlRequestDTO,
                                                   String slackUserId) {
        ddlValidator.validateRenameColumn(databaseConnectionInfo, ddlRequestDTO);
        String createTableStatement = ddlService.renameColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.extractCommandType(),
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                slackUserId,
                LocalDateTime.now()), ddlRequestDTO);

        return new RenameColumnDDLResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                ddlRequestDTO.getSchemaName(),
                ddlRequestDTO.getTableName(),
                createTableStatement);
    }

}
