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
    private final SlackUserService slackUserService;

    // 승인 컨센서스 숫자 - ex.승인이 반려보다 X 이상 많으면 승인
    private final static int APPROVAL_CONSENSUS_NUMBER = 1;

    @Transactional(readOnly = true)
    public MonitorTargetDatabase findMonitorTargetDatabase(Long id) {
        return monitorTargetDatabaseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " : 해당 ID의 모니터링 대상 DB가 없습니다."));
    }

    @Transactional
    public SlackDatabaseRequest saveSlackDatabaseRequest(SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO) {
        SlackDatabaseRequest slackDatabaseRequest;
        MonitorTargetDatabase monitorTargetDatabase = slackDatabaseIntegratedDTO.getDatabaseConnectionInfo().toMonitorTargetDatabase();
        if (monitorTargetDatabase.getId() == null) {
            if (!monitorTargetDatabase.isMonitorTarget()) {
                throw new IllegalStateException("관리 대상 DB가 아닙니다. 등록 후 사용해주세요.");
            }
            monitorTargetDatabaseRepository.save(monitorTargetDatabase);
        }

        SlackUser slackUser = slackUserService.findSlackUser(slackDatabaseIntegratedDTO.getRequestUserSlackId());
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
                    SlackDatabaseRequest.ExecuteStatus.VOTING);
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info(Arrays.toString(e.getStackTrace()));
            throw new IllegalArgumentException("SlackDatabaseRequest Json.toJson() 오류 발생");
        }

        return slackDatabaseRequestRepository.save(slackDatabaseRequest);
    }

    @Transactional(readOnly = true)
    public SlackDatabaseIntegratedDTO findSlackDatabaseRequest(String requestUUID) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUuid(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + ": 해당 UUID의 Database Request가 없습니다."));
        SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO;
        try {
            slackDatabaseIntegratedDTO = new SlackDatabaseIntegratedDTO(DatabaseConnectionInfo.of(slackDatabaseRequest.getMonitorTargetDatabase()),
                    slackDatabaseRequest.getSlackUser().getUserSlackId(),
                    slackDatabaseRequest.getCommandType(),
                    slackDatabaseRequest.getRequestDtoClassType().replace("class ", ""),
                    (RequestDTO) JsonUtil.toObject(slackDatabaseRequest.getRequestDto(), Class.forName(slackDatabaseRequest.getRequestDtoClassType())),
                    slackDatabaseRequest.getRequestUuid(),
                    slackDatabaseRequest.getRequestContent(),
                    slackDatabaseRequest.getRequestDescription(),
                    slackDatabaseRequest.getExecuteDatetime());
        } catch (Exception e) {
            log.info(e.getMessage());
            log.info(Arrays.toString(e.getStackTrace()));
            log.info("slackDatabaseRequest: {}", slackDatabaseRequest);
            throw new IllegalArgumentException("SlackDatabaseRequest DTO 변환 실패");
        }

        return slackDatabaseIntegratedDTO;
    }

    @Transactional(readOnly = true)
    public SlackDatabaseRequest.ExecuteStatus findExecuteStatus(String requestUUID) {
        SlackDatabaseRequest.ExecuteStatus executeStatus = slackDatabaseRequestRepository.findExecuteStatusByRequestUuid(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + " : 해당 UUID의 DB 요청이 존재하지 않습니다."));
        return executeStatus;
    }

    // DatabaseRequest 의 상태가 승인/반려/보류 액션을 핸들링 할 수 있는 상태인지 검사
    @Transactional(readOnly = true)
    public boolean isSlackDatabaseRequestVotableStatus(String requestUUID) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUuid(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + " : 해당 UUID의 DB 요청이 존재하지 않습니다."));

        if (!slackDatabaseRequest.isVotableStatus()) {
            return false;
        }
        return true;
    }

    @Transactional
    public void complete(String requestUUID) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUuid(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + " : 해당 UUID의 DB 요청이 존재하지 않습니다."));
        slackDatabaseRequest.complete();
    }

    @Transactional
    public void responseToRequest(String requestUUID, String slackUserId, SlackDatabaseRequestApproval.ResponseType responseType) {
        SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestRepository.findOneByRequestUuid(requestUUID)
                .orElseThrow(() -> new IllegalStateException(requestUUID + " : 해당 UUID의 DB 요청이 존재하지 않습니다."));

        // 이미 투표한 요청인지 확인
        List<SlackDatabaseRequestApproval> approvals = slackDatabaseReQuestApprovalRepository.findByDatabaseRequestUUID(requestUUID);
        for (SlackDatabaseRequestApproval approval : approvals) {
            if (approval.getSlackUser().getUserSlackId().equals(slackUserId)) {
                throw new IllegalArgumentException("해당 유저가 이미 응답한 요청입니다.");
            }
        }

        // 새로운 응답 생성
        SlackDatabaseRequestApproval response = new SlackDatabaseRequestApproval(null,
                slackDatabaseRequest,
                slackUserService.findSlackUser(slackUserId),
                responseType,
                "TODO",
                LocalDateTime.now());

        slackDatabaseReQuestApprovalRepository.save(response);

        // 컨센서스 계산 및 요청 상태 변경
        Map<SlackDatabaseRequestApproval.ResponseType, List<SlackDatabaseRequestApproval>> approvalMap = slackDatabaseReQuestApprovalRepository.findByDatabaseRequestUUID(requestUUID)
                .stream()
                .collect(Collectors.groupingBy(SlackDatabaseRequestApproval::getResponseType));
        int acceptConsensusCount = approvalMap.getOrDefault(SlackDatabaseRequestApproval.ResponseType.ACCEPT, List.of()).size() -
                approvalMap.getOrDefault(SlackDatabaseRequestApproval.ResponseType.DENY, List.of()).size();

        if (acceptConsensusCount >= APPROVAL_CONSENSUS_NUMBER) {
            slackDatabaseRequest.accept();
        } else if (acceptConsensusCount <= -APPROVAL_CONSENSUS_NUMBER) {
            slackDatabaseRequest.deny();
        }
    }

    @Transactional(readOnly = true)
    public List<SlackDatabaseIntegratedDTO> findInProgressSlackDatabaseRequests() {
        return slackDatabaseRequestRepository.findInProgress()
                .stream()
                .map(s -> {
                    try {
                        return new SlackDatabaseIntegratedDTO(DatabaseConnectionInfo.of(s.getMonitorTargetDatabase()),
                                s.getSlackUser().getUserSlackId(),
                                s.getCommandType(),
                                s.getRequestDtoClassType(),
                                (RequestDTO) JsonUtil.toObject(s.getRequestDto(), Class.forName(s.getRequestDtoClassType())),
                                s.getRequestUuid(),
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
