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
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.view.BlockPageManager;
import zzangmin.db_automation.view.slackrequestpage.SlackRequestMessagePage;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DatabaseRequestExecutor {

    private static final int EXECUTE_DELAY = 10000;

    private final BlockPageManager blockPageManager;
    private final SlackDatabaseRequestService slackDatabaseRequestService;
    private final SlackService slackService;

    @Scheduled(fixedDelay = EXECUTE_DELAY)
    public void execute() {
        List<SlackDatabaseIntegratedDTO> notCompletedSlackDatabaseRequestDTOs = slackDatabaseRequestService.findNotCompletedSlackDatabaseRequests();

        for (SlackDatabaseIntegratedDTO notCompletedSlackDatabaseRequestDTO : notCompletedSlackDatabaseRequestDTOs) {
            // TODO: execute_datetime 기반 실행, accept/deny count 검증
            String findRequestUUID = notCompletedSlackDatabaseRequestDTO.getRequestUUID();
            SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO = slackDatabaseRequestService.findSlackDatabaseRequest(findRequestUUID);
            DatabaseConnectionInfo findDatabaseConnectionInfo = slackDatabaseIntegratedDTO.getDatabaseConnectionInfo();
            DatabaseRequestCommandGroup.CommandType findCommandType = slackDatabaseIntegratedDTO.getCommandType();
            RequestDTO findRequestDTO = slackDatabaseIntegratedDTO.getRequestDTO();
            String slackUserId = slackDatabaseIntegratedDTO.getRequestUserSlackId();


            List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(findCommandType, findRequestDTO);
            List<LayoutBlock> startMessageBlocks = SlackRequestMessagePage.findRequestExecuteStartMessageBlocks(findCommandType, findDatabaseConnectionInfo, slackUserId, contentBlocks);
            slackService.sendBlockMessage(startMessageBlocks);

            List<LayoutBlock> resultBlocks = new ArrayList<>();
            try {
                String executeResult = blockPageManager.execute(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId);
                List<LayoutBlock> requestEndMessageBlocks = SlackRequestMessagePage.findRequestEndMessage(findCommandType, findDatabaseConnectionInfo, findRequestDTO, findRequestUUID, executeResult);
                resultBlocks = requestEndMessageBlocks;
                slackDatabaseRequestService.complete(findRequestUUID);
            } catch (Exception e) {
                List<LayoutBlock> requestFailMessageBlocks = SlackRequestMessagePage.findRequestFailMessageBlocks(findCommandType, findDatabaseConnectionInfo, findRequestUUID, e.getMessage(), contentBlocks);
                resultBlocks = requestFailMessageBlocks;
            }

            slackService.sendBlockMessage(resultBlocks);
        }
    }
}
