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

    // TODO: 인증/인가

    @GetMapping("/ddl/validate")
    public String validCommand(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                               @RequestBody DDLRequestDTO ddlRequestDTO) {
        ddlValidator.validateDDLRequest(databaseConnectionInfo, ddlRequestDTO);
        return "ok";
    }

    // TODO: add column auto_increment block
    @PutMapping("/ddl/column")
    public AddColumnResponseDTO addColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                            @RequestBody AddColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateAddColumn(databaseConnectionInfo, ddlRequestDTO);
        AddColumnResponseDTO addColumnResponseDTO = ddlService.addColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return addColumnResponseDTO;
    }

    @PatchMapping("/ddl/column")
    public AlterColumnResponseDTO alterColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                              @RequestBody AlterColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateAlterColumn(databaseConnectionInfo, ddlRequestDTO);
        AlterColumnResponseDTO alterColumnResponseDTO = ddlService.alterColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return alterColumnResponseDTO;
    }

    // TODO: primary key 필수 포함, id 컬럼 포함
    @PutMapping("/ddl/index")
    public CreateIndexResponseDTO createIndex(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestBody CreateIndexRequestDTO ddlRequestDTO) {
        ddlValidator.validateCreateIndex(databaseConnectionInfo, ddlRequestDTO);
        CreateIndexResponseDTO createIndexResponseDTO = ddlService.createIndex(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return createIndexResponseDTO;
    }

    @PutMapping("/ddl/table")
    public CreateTableResponseDTO createTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestBody CreateTableRequestDTO ddlRequestDTO) throws InterruptedException {
        ddlValidator.validateCreateTable(databaseConnectionInfo, ddlRequestDTO);
        CreateTableResponseDTO createTableResponseDTO = ddlService.createTable(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return createTableResponseDTO;
    }

    // TODO: rename -> delete
    @DeleteMapping("/ddl/column")
    public DeleteColumnResponseDTO deleteColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                @RequestBody DeleteColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateDeleteColumn(databaseConnectionInfo, ddlRequestDTO);
        DeleteColumnResponseDTO deleteColumnResponseDTO = ddlService.deleteColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);        return deleteColumnResponseDTO;
    }

    @PatchMapping("/ddl/varchar")
    public ExtendVarcharColumnResponseDTO extendVarcharColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                              @RequestBody ExtendVarcharColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateExtendVarchar(databaseConnectionInfo, ddlRequestDTO);
        ExtendVarcharColumnResponseDTO extendVarcharColumnResponseDTO = ddlService.extendVarcharColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);
        return extendVarcharColumnResponseDTO;
    }

    @PatchMapping("/ddl/column/name")
    public RenameColumnResponseDTO renameColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                @RequestBody RenameColumnRequestDTO ddlRequestDTO) {
        ddlValidator.validateRenameColumn(databaseConnectionInfo, ddlRequestDTO);
        RenameColumnResponseDTO renameColumnResponseDTO = ddlService.renameColumn(databaseConnectionInfo, ddlRequestDTO);
        changeHistoryService.addChangeHistory(new CreateChangeHistoryRequestDTO(ddlRequestDTO.getCommandType(), databaseConnectionInfo.getDatabaseName(), ddlRequestDTO.getSchemaName(), ddlRequestDTO.getTableName(), "test@gmail.com", LocalDateTime.now()), ddlRequestDTO);
        return renameColumnResponseDTO;
    }

//
//    @PatchMapping"/ddl/index/name")
//
//    @PatchMapping("/ddl/column/comment")
//
//    @PatchMapping("/ddl/table/comment")



}
