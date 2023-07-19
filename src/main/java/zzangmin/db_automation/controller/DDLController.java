package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.dto.response.*;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
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
    public String validCommand(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                               @RequestBody DDLRequestDTO ddlRequestDTO) {
        ddlValidator.validateDDLRequest(databaseConnectionInfo, ddlRequestDTO);
        return "ok";
    }

    @PutMapping("/ddl/column")
    public AddColumnDDLResponseDTO addColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                             @RequestBody AddColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateAddColumn(databaseConnectionInfo, ddlRequestDTO);
        AddColumnDDLResponseDTO addColumnResponseDTO = ddlService.addColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return addColumnResponseDTO;
    }

    @PatchMapping("/ddl/column")
    public AlterColumnDDLResponseDTO alterColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                 @RequestBody AlterColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateAlterColumn(databaseConnectionInfo, ddlRequestDTO);
        AlterColumnDDLResponseDTO alterColumnResponseDTO = ddlService.alterColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return alterColumnResponseDTO;
    }

    @PutMapping("/ddl/index")
    public CreateIndexDDLResponseDTO createIndex(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                 @RequestBody CreateIndexRequestDTO ddlRequestDTO) {
        ddlValidator.validateCreateIndex(databaseConnectionInfo, ddlRequestDTO);
        CreateIndexDDLResponseDTO createIndexResponseDTO = ddlService.createIndex(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return createIndexResponseDTO;
    }

    @PutMapping("/ddl/table")
    public CreateTableDDLResponseDTO createTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                 @RequestBody CreateTableRequestDTO ddlRequestDTO) throws InterruptedException {
        ddlValidator.validateCreateTable(databaseConnectionInfo, ddlRequestDTO);
        CreateTableDDLResponseDTO createTableResponseDTO = ddlService.createTable(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return createTableResponseDTO;
    }

    @DeleteMapping("/ddl/column")
    public DeleteColumnDDLResponseDTO deleteColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                   @RequestBody DeleteColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateDeleteColumn(databaseConnectionInfo, ddlRequestDTO);
        DeleteColumnDDLResponseDTO deleteColumnResponseDTO = ddlService.deleteColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return deleteColumnResponseDTO;
    }

    @PatchMapping("/ddl/varchar")
    public ExtendVarcharColumnDDLResponseDTO extendVarcharColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                                 @RequestBody ExtendVarcharColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateExtendVarchar(databaseConnectionInfo, ddlRequestDTO);
        ExtendVarcharColumnDDLResponseDTO extendVarcharColumnResponseDTO = ddlService.extendVarcharColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);
        return extendVarcharColumnResponseDTO;
    }

    @PatchMapping("/ddl/column/name")
    public RenameColumnDDLResponseDTO renameColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                   @RequestBody RenameColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateRenameColumn(databaseConnectionInfo, ddlRequestDTO);
        RenameColumnDDLResponseDTO renameColumnResponseDTO = ddlService.renameColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);
        return renameColumnResponseDTO;
    }

}
