package zzangmin.db_automation.schedule.dbrequestexecutor;

import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.request.SlackDatabaseIntegratedDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackDatabaseRequestService;
import zzangmin.db_automation.service.SlackMessageService;
import zzangmin.db_automation.view.BlockPageManager;
import zzangmin.db_automation.view.slackrequestpage.SlackRequestMessagePage;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DatabaseRequestExecutor {

    private static final int EXECUTE_DELAY = 10000000;

    private final BlockPageManager blockPageManager;
    private final SlackDatabaseRequestService slackDatabaseRequestService;
    private final SlackMessageService slackMessageService;

    @Scheduled(fixedDelay = EXECUTE_DELAY)
    public void execute() {
        List<SlackDatabaseIntegratedDTO> notCompletedSlackDatabaseRequestDTOs = slackDatabaseRequestService.findInProgressSlackDatabaseRequests();

        for (SlackDatabaseIntegratedDTO notCompletedSlackDatabaseRequestDTO : notCompletedSlackDatabaseRequestDTOs) {
            // TODO: execute_datetime 기반 실행, accept/deny count 검증
            String findRequestUUID = notCompletedSlackDatabaseRequestDTO.getRequestUUID();
            SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO = slackDatabaseRequestService.findSlackDatabaseRequest(findRequestUUID);
            DatabaseConnectionInfo findDatabaseConnectionInfo = slackDatabaseIntegratedDTO.getDatabaseConnectionInfo();
            DatabaseRequestCommandGroup.CommandType findCommandType = slackDatabaseIntegratedDTO.getCommandType();
            RequestDTO findRequestDTO = slackDatabaseIntegratedDTO.getRequestDTO();
            String slackUserId = slackDatabaseIntegratedDTO.getRequestUserSlackId();

            List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(findCommandType, findRequestDTO);
            List<LayoutBlock> startMessageBlocks = SlackRequestMessagePage.findRequestExecuteStartMessageBlocks(findCommandType, findDatabaseConnectionInfo, findRequestUUID, contentBlocks);
            slackMessageService.sendBlockMessage(startMessageBlocks);

            List<LayoutBlock> resultBlocks = new ArrayList<>();
            try {
                String executeResult = blockPageManager.execute(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId);
                resultBlocks = SlackRequestMessagePage.findRequestEndMessage(findCommandType, findDatabaseConnectionInfo, contentBlocks, findRequestUUID, executeResult);
                slackDatabaseRequestService.complete(findRequestUUID);
            } catch (Exception e) {
                resultBlocks = SlackRequestMessagePage.findRequestFailMessageBlocks(findCommandType, findDatabaseConnectionInfo, contentBlocks, findRequestUUID, e.getMessage());
            }

            slackMessageService.sendBlockMessage(resultBlocks);
        }
    }
}
