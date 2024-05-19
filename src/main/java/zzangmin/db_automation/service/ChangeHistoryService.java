package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.entity.ChangeHistory;
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
                .changeContentSql(ddlRequestDTO.toSQL())
                .build();
        changeHistoryRepository.save(changeHistory);
        log.info("ChangeHistoryService.addChangeHistory(): {}", changeHistory);
    }

    @Transactional(readOnly = true)
    public List<ChangeHistory> findChangeHistories(String databaseIdentifier, String schemaName, String tableName) {
        return changeHistoryRepository.findByDatabaseIdentifierAndSchemaNameAndTableName(databaseIdentifier, schemaName, tableName);
    }
}
