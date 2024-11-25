package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.request.SlackDatabaseIntegratedDTO;
import zzangmin.db_automation.entity.MonitorTargetDatabase;
import zzangmin.db_automation.entity.SlackDatabaseRequest;
import zzangmin.db_automation.repository.MonitorTargetDatabaseRepository;
import zzangmin.db_automation.repository.SlackDatabaseRequestRepository;
import zzangmin.db_automation.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackDatabaseRequestService {
    private final SlackDatabaseRequestRepository slackDatabaseRequestRepository;
    private final MonitorTargetDatabaseRepository monitorTargetDatabaseRepository;

    @Transactional(readOnly = true)
    public MonitorTargetDatabase findMonitorTargetDatabase(Long id) {
        return monitorTargetDatabaseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " : 해당 ID의 모니터링 대상 DB가 없습니다."));;
    }

    @Transactional
    public SlackDatabaseRequest saveSlackDatabaseRequest(SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO) {
        SlackDatabaseRequest slackDatabaseRequest;
        MonitorTargetDatabase monitorTargetDatabase = slackDatabaseIntegratedDTO.getDatabaseConnectionInfo().toMonitorTargetDatabase();
        try {
            slackDatabaseRequest = new SlackDatabaseRequest(null,
                    monitorTargetDatabase,
                    slackDatabaseIntegratedDTO.getRequestUserSlackId(),
                    slackDatabaseIntegratedDTO.getCommandType(),
                    slackDatabaseIntegratedDTO.getRequestDTOClassType(),
                    JsonUtil.toJson(slackDatabaseIntegratedDTO.getRequestDTO()),
                    slackDatabaseIntegratedDTO.getRequestUUID(),
                    slackDatabaseIntegratedDTO.getRequestContent(),
                    slackDatabaseIntegratedDTO.getRequestDescription(),
                    LocalDateTime.now(),
                    false);
        } catch (Exception e) {
            throw new IllegalArgumentException("SlackDatabaseRequest Json.toJson() 오류 발생");
        }

        return slackDatabaseRequestRepository.save(slackDatabaseRequest);
    }

    @Transactional(readOnly = true)
    public SlackDatabaseIntegratedDTO findSlackDatabaseRequest(String requestUUID) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUUID(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + ": 해당 UUID의 Database Request가 없습니다."));
        SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO;
        try {
            slackDatabaseIntegratedDTO = new SlackDatabaseIntegratedDTO(DatabaseConnectionInfo.of(slackDatabaseRequest.getMonitorTargetDatabase()),
                    slackDatabaseRequest.getRequestUserSlackId(),
                    slackDatabaseRequest.getCommandType(),
                    slackDatabaseRequest.getRequestDTOClassType(),
                    (RequestDTO) JsonUtil.toObject(slackDatabaseRequest.getRequestDTO(), Class.forName(slackDatabaseRequest.getRequestDTOClassType())),
                    slackDatabaseRequest.getRequestUUID(),
                    slackDatabaseRequest.getRequestContent(),
                    slackDatabaseRequest.getRequestDescription());
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info(e.getStackTrace());
            throw new IllegalArgumentException("SlackDatabaseRequest DTO 변환 실패");
        }

        return slackDatabaseIntegratedDTO;
    }
}
