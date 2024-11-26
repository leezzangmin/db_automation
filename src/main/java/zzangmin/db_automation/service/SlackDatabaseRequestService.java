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
import zzangmin.db_automation.entity.SlackDatabaseRequestApproval;
import zzangmin.db_automation.entity.SlackUser;
import zzangmin.db_automation.repository.MonitorTargetDatabaseRepository;
import zzangmin.db_automation.repository.SlackDatabaseReQuestApprovalRepository;
import zzangmin.db_automation.repository.SlackDatabaseRequestRepository;
import zzangmin.db_automation.repository.SlackUserRepository;
import zzangmin.db_automation.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackDatabaseRequestService {
    private final SlackDatabaseRequestRepository slackDatabaseRequestRepository;
    private final MonitorTargetDatabaseRepository monitorTargetDatabaseRepository;
    private final SlackDatabaseReQuestApprovalRepository slackDatabaseReQuestApprovalRepository;
    private final SlackUserRepository slackUserRepository;

    private final static int APPROVAL_CONSENSUS_NUMBER = 2;

    @Transactional(readOnly = true)
    public MonitorTargetDatabase findMonitorTargetDatabase(Long id) {
        return monitorTargetDatabaseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " : 해당 ID의 모니터링 대상 DB가 없습니다."));;
    }

    @Transactional
    public SlackDatabaseRequest saveSlackDatabaseRequest(SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO) {
        SlackDatabaseRequest slackDatabaseRequest;
        MonitorTargetDatabase monitorTargetDatabase = slackDatabaseIntegratedDTO.getDatabaseConnectionInfo().toMonitorTargetDatabase();
        SlackUser slackUser = slackUserRepository.findByUserSlackId(slackDatabaseIntegratedDTO.getRequestUserSlackId())
                .orElseThrow(() -> new IllegalStateException(slackDatabaseIntegratedDTO.getRequestUserSlackId() + " : 해당 slack user ID 유저가 테이블에 존재하지 않습니다."));
        try {
            slackDatabaseRequest = new SlackDatabaseRequest(null,
                    monitorTargetDatabase,
                    slackUser,
                    slackDatabaseIntegratedDTO.getCommandType(),
                    slackDatabaseIntegratedDTO.getRequestDTOClassType(),
                    JsonUtil.toJson(slackDatabaseIntegratedDTO.getRequestDTO()),
                    slackDatabaseIntegratedDTO.getRequestUUID(),
                    slackDatabaseIntegratedDTO.getRequestContent(),
                    slackDatabaseIntegratedDTO.getRequestDescription(),
                    LocalDateTime.now(),
                    slackDatabaseIntegratedDTO.getExecuteDatetime(),
                    false);
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info(Arrays.toString(e.getStackTrace()));
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
                    slackDatabaseRequest.getSlackUser().getUserSlackId(),
                    slackDatabaseRequest.getCommandType(),
                    slackDatabaseRequest.getRequestDTOClassType(),
                    (RequestDTO) JsonUtil.toObject(slackDatabaseRequest.getRequestDTO(), Class.forName(slackDatabaseRequest.getRequestDTOClassType())),
                    slackDatabaseRequest.getRequestUUID(),
                    slackDatabaseRequest.getRequestContent(),
                    slackDatabaseRequest.getRequestDescription(),
                    slackDatabaseRequest.getExecuteDatetime());
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info(Arrays.toString(e.getStackTrace()));
            throw new IllegalArgumentException("SlackDatabaseRequest DTO 변환 실패");
        }

        return slackDatabaseIntegratedDTO;
    }

    // DatabaseRequest 의 상태가 승인/반려/보류 의 액션을 핸들링 할 수 있는 상태인지 검사
    @Transactional(readOnly = true)
    public boolean isSlackDatabaseRequestAcceptableStatus(String requestUUID, String acceptSlackId) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUUID(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + " : 해당 UUID의 DB 요청이 존재하지 않습니다."));

        // 요청자와 응답자가 같으면 false
        if (slackDatabaseRequest.getSlackUser().getUserSlackId().equals(acceptSlackId)) {
            return false;
        }

        if (slackDatabaseRequest.getIsComplete()) {
            return false;
        }

        Map<SlackDatabaseRequestApproval.responseType, List<SlackDatabaseRequestApproval>> approvals = slackDatabaseReQuestApprovalRepository.findByDatabaseRequestUUID(requestUUID)
                .stream()
                .collect(Collectors.groupingBy(SlackDatabaseRequestApproval::getResponseType));

        int consensusCount = approvals.get(SlackDatabaseRequestApproval.responseType.ACCEPT).size() - approvals.get(SlackDatabaseRequestApproval.responseType.DENY).size();
        if (consensusCount >= APPROVAL_CONSENSUS_NUMBER) {
            return true;
        }

        slackDatabaseRequest.complete();
        return false;
    }

    @Transactional
    public void complete(String requestUUID) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUUID(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + " : 해당 UUID의 DB 요청이 존재하지 않습니다."));
        slackDatabaseRequest.complete();
    }

    @Transactional(readOnly = true)
    public List<SlackDatabaseIntegratedDTO> findNotCompletedSlackDatabaseRequests() {
        return slackDatabaseRequestRepository.findNotCompleted()
                .stream()
                .map(s -> {
                    try {
                        return new SlackDatabaseIntegratedDTO(DatabaseConnectionInfo.of(s.getMonitorTargetDatabase()),
                                s.getSlackUser().getUserSlackId(),
                                s.getCommandType(),
                                s.getRequestDTOClassType(),
                                (RequestDTO) JsonUtil.toObject(s.getRequestDTO(), Class.forName(s.getRequestDTOClassType())),
                                s.getRequestUUID(),
                                s.getRequestContent(),
                                s.getRequestDescription(),
                                s.getExecuteDatetime());
                    } catch (Exception e) {
                        log.info(e.getMessage());
                        log.info(Arrays.toString(e.getStackTrace()));
                        throw new IllegalArgumentException("SlackDatabaseRequest DTO 변환 실패");
                    }
                })
                .toList();
    }
}
