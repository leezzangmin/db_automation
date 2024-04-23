package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.entity.ChangeHistory;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.repository.ChangeHistoryRepository;

import java.util.List;

// 변경관리 이력 쌓는 서비스

@Slf4j
@RequiredArgsConstructor
@Service
public class ChangeHistoryService {
    private final ChangeHistoryRepository changeHistoryRepository;

    @Transactional
    public void addChangeHistory(CreateChangeHistoryRequestDTO createChangeHistoryRequestDTO, DDLRequestDTO ddlRequestDTO) {
        ChangeHistory changeHistory = ChangeHistory.builder()
                .commandType(createChangeHistoryRequestDTO.getCommandType())
                .databaseIdentifier(createChangeHistoryRequestDTO.getDatabaseIdentifier())
                .schemaName(createChangeHistoryRequestDTO.getSchemaName())
                .tableName(createChangeHistoryRequestDTO.getTableName())
                .doer(createChangeHistoryRequestDTO.getDoer())
                .doDateTime(createChangeHistoryRequestDTO.getDoDateTime())
                .changeContent(generateChangeContent(createChangeHistoryRequestDTO, ddlRequestDTO))
                .build();
        changeHistoryRepository.save(changeHistory);
        log.info("ChangeHistoryService.addChangeHistory(): {}", changeHistory);
    }

    @Transactional(readOnly = true)
    public List<ChangeHistory> findChangeHistories(String databaseIdentifier, String schemaName, String tableName) {
        return changeHistoryRepository.findByDatabaseIdentifierAndSchemaNameAndTableName(databaseIdentifier, schemaName, tableName);
    }

    private String generateChangeContent(CreateChangeHistoryRequestDTO createChangeHistoryRequestDTO, DDLRequestDTO ddlRequestDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("schemaName: ");
        sb.append(createChangeHistoryRequestDTO.getSchemaName());
        sb.append(", tableName: ");
        sb.append(createChangeHistoryRequestDTO.getTableName());
        sb.append(", commandType: ");
        sb.append(ddlRequestDTO.getCommandType());

        if (ddlRequestDTO.getCommandType().equals(CommandType_old.CREATE_INDEX)) {
            CreateIndexRequestDTO dto = (CreateIndexRequestDTO) ddlRequestDTO;
            sb.append(", indexName: ");
            sb.append(dto.getIndexName());
            sb.append(", columnNames: ");
            sb.append(dto.getColumnNames());
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.CREATE_TABLE)) {
            CreateTableRequestDTO dto = (CreateTableRequestDTO) ddlRequestDTO;
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.ADD_COLUMN)) {
            AddColumnRequestDTO dto = (AddColumnRequestDTO) ddlRequestDTO;
            sb.append(", column: ");
            sb.append(dto.getColumn());
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.ALTER_COLUMN)) {
            AlterColumnRequestDTO dto = (AlterColumnRequestDTO) ddlRequestDTO;
            sb.append(", after: ");
            sb.append(dto.getAfterColumn());
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.DELETE_COLUMN)) {
            DeleteColumnRequestDTO dto = (DeleteColumnRequestDTO) ddlRequestDTO;
            sb.append(", delteColumnName: ");
            sb.append(dto.getColumnName());
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.EXTEND_VARCHAR_COLUMN)) {
            ExtendVarcharColumnRequestDTO dto = (ExtendVarcharColumnRequestDTO) ddlRequestDTO;
            sb.append(", extendColumnName: ");
            sb.append(dto.getTargetColumnName());
            sb.append(", extendColumnSize: ");
            sb.append(dto.getExtendSize());
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.RENAME_COLUMN)) {
            RenameColumnRequestDTO dto = (RenameColumnRequestDTO) ddlRequestDTO;
            sb.append(", beforeColumnName: ");
            sb.append(dto.getBeforeColumnName());
            sb.append(", afterColumnName: ");
            sb.append(dto.getAfterColumnName());
        } else {
            throw new IllegalArgumentException("지원하지 않는 명령");
        }
        sb.append(", executeTime: ");
        sb.append(createChangeHistoryRequestDTO.getDoDateTime());
        sb.append(", doer: ");
        sb.append(createChangeHistoryRequestDTO.getDoer());
        return sb.toString();
    }
}
