package zzangmin.db_automation.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.dto.request.CreateChangeHistoryRequestDTO;
import zzangmin.db_automation.entity.ChangeHistory;
import zzangmin.db_automation.repository.ChangeHistoryRepository;

// 변경관리 이력 쌓는 서비스

@Slf4j
@RequiredArgsConstructor
@Service
public class ChangeHistoryService {
    private final ChangeHistoryRepository changeHistoryRepository;

    @Transactional
    public void addChangeHistory(CreateChangeHistoryRequestDTO createChangeHistoryRequestDTO) {
        ChangeHistory changeHistory = ChangeHistory.builder()
                .commandType(createChangeHistoryRequestDTO.getCommandType())
                .databaseIdentifier(createChangeHistoryRequestDTO.getDatabaseIdentifier())
                .schemaName(createChangeHistoryRequestDTO.getSchemaName())
                .tableName(createChangeHistoryRequestDTO.getTableName())
                .doer(createChangeHistoryRequestDTO.getDoer())
                .doDateTime(createChangeHistoryRequestDTO.getDoDateTime())
                .changeeeeeeeee(createChangeHistoryRequestDTO.getChangeeeeeeeee())
                .build();
        changeHistoryRepository.save(changeHistory);
        log.info("ChangeHistoryService.addChangeHistory() : {}", changeHistory);
    }
}
