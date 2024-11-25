package zzangmin.db_automation.view.slackrequestpage;

import com.slack.api.methods.request.chat.ChatUpdateRequest;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.SlackConfig;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.request.SlackDatabaseIntegratedDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.entity.MonitorTargetDatabase;
import zzangmin.db_automation.entity.SlackDatabaseRequest;
import zzangmin.db_automation.repository.MonitorTargetDatabaseRepository;
import zzangmin.db_automation.repository.SlackDatabaseRequestRepository;
import zzangmin.db_automation.service.SlackDatabaseRequestService;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.util.JsonUtil;
import zzangmin.db_automation.view.BasicBlockFactory;
import zzangmin.db_automation.view.BlockPage;
import zzangmin.db_automation.view.SlackConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

@Slf4j
@RequiredArgsConstructor
@Component
public class SlackRequestMessagePage implements BlockPage {

    private final SlackDatabaseRequestService slackDatabaseRequestService;

    @Override
    public List<LayoutBlock> generateBlocks() {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return false;
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommunicationBlockIds
                .isMember(actionId);
    }

    @Override
    public void handleViewAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        throw new IllegalArgumentException("View Action 미지원 page");

    }

    @Override
    public void handleMessageAction(String actionId, String userId, Message message) {
//        if (actionId.equals(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)) {
//            handleAccept(message, userId);
//        } else if (actionId.equals(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)) {
//            handleDeny(message, userId);
//        } else {
//            throw new IllegalArgumentException("미지원 actionId: " + actionId);
//        }
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public String execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        throw new IllegalArgumentException("미지원 page");
    }

    public static List<LayoutBlock> findSubmissionRequestMessageBlocks(DatabaseConnectionInfo databaseConnectionInfo,
                                                                DatabaseRequestCommandGroup.CommandType commandType,
                                                                String slackUserId,
                                                                String requestUUID,
                                                                List<LayoutBlock> requestContentBlocks) {
        List<LayoutBlock> requestBlocks = new ArrayList<>();

        // 헤더
        requestBlocks.add(BasicBlockFactory.findHeaderBlock(":rocket: Database Request Has Arrived !", "requestblock0"));

        // 요청 ID
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Request UUID:* `" + requestUUID + "`",
                "requestblock233"));

        // Target DB 정보
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Target Database:*" + databaseConnectionInfo.databaseSummary(),
                "requestblock1"));

        // 요청 커맨드 종류
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "requestblock2"));
        requestBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 내용 Blocks
        requestBlocks.addAll(requestContentBlocks);

        // 요청자 slack id 멘션
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Doer:* <@" + slackUserId + ">",
                "requestblock3"));
        requestBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 시간
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Time:* `" + LocalDateTime.now() + "`",
                "requestblock4"));

        // 승인/반려 버튼
        requestBlocks.add(
                actions(actions -> actions
                        .elements(asElements(
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
                                        .value(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)
                                        .style("primary")
                                        .actionId(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)
                                ),
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("반려")))
                                        .value(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)
                                        .style("danger")
                                        .actionId(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)
                                )))
                        .blockId(SlackConstants.CommunicationBlockIds.commandRequestAcceptDenyButtonBlockId)));

        return requestBlocks;
    }

    public static List<LayoutBlock> findRequestFailMessageBlocks(DatabaseRequestCommandGroup.CommandType commandType,
                                                    DatabaseConnectionInfo databaseConnectionInfo,
                                                    RequestDTO requestDTO,
                                                    String slackUserId,
                                                    String requestUUID,
                                                    String exceptionMessage) {
        List<LayoutBlock> failMessageBlocks = new ArrayList<>();
        failMessageBlocks.add(BasicBlockFactory.findHeaderBlock(":x: Request Execution Failed !", "RequestExecuteFailed"));

        // 요청 ID
        failMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Request UUID:* `" + requestUUID + "`",
                "requestblock100"));

        // Target DB 정보
        failMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Target Database:*" + databaseConnectionInfo.databaseSummary(),
                "requestblock101"));

        // 요청 커맨드 종류
        failMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "requestblock102"));
        failMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 내용
        List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(commandType, requestDTO);
        failMessageBlocks.addAll(contentBlocks);

        // 작업 실패 이유
        failMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Due to:*" + exceptionMessage,
                "requestblock103"));
        failMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 작업 실패 시간
        failMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Execution Start Time:* `" + LocalDateTime.now() + "`",
                "requestblock104"));
        slackService.sendBlockMessage(failMessageBlocks);

        return null;
    }

    public static List<LayoutBlock> findRequestExecuteStartMessageBlocks(DatabaseRequestCommandGroup.CommandType commandType, DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId, String requestUUID) {
        List<LayoutBlock> startMessageBlocks = new ArrayList<>();
        startMessageBlocks.add(BasicBlockFactory.findHeaderBlock(":arrow_forward: Request Execution Started !", "RequestExecuteStarted"));

        // 요청 ID
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Request UUID:* `" + requestUUID + "`",
                "requestblock100"));

        // Target DB 정보
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Target Database:*" + databaseConnectionInfo.databaseSummary(),
                "requestblock101"));

        // 요청 커맨드 종류
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "requestblock102"));
        startMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 내용
        List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(commandType, requestDTO);
        startMessageBlocks.addAll(contentBlocks);

        // 작업 시작 시간
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Execution Start Time:* `" + LocalDateTime.now() + "`",
                "requestblock104"));
        slackService.sendBlockMessage(startMessageBlocks);

        return null;
    }

    public static List<LayoutBlock> findRequestAcceptMessageBlocks(DatabaseRequestCommandGroup.CommandType commandType,
                                                DatabaseConnectionInfo databaseConnectionInfo,
                                                RequestDTO requestDTO,
                                                String slackUserId,
                                                String requestUUID) {
        List<LayoutBlock> startMessageBlocks = new ArrayList<>();
        startMessageBlocks.add(BasicBlockFactory.findHeaderBlock(":ghost: Request Accepted !", "RequestAccepted"));

        // 요청 ID
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Request UUID:* `" + requestUUID + "`",
                "requestblock233"));

        // Target DB 정보
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Target Database:*" + databaseConnectionInfo.databaseSummary(),
                "requestblock1"));

        // 요청 커맨드 종류
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "requestblock2"));
        startMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 내용
        List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(commandType, requestDTO);
        startMessageBlocks.addAll(contentBlocks);

        // 승인자 slack id 멘션
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Accepted By:* <@" + slackUserId + ">",
                "requestblock3"));
        startMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 승인 시간
        startMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Accepted Time:* `" + LocalDateTime.now() + "`",
                "requestblock4"));
        slackService.sendBlockMessage(startMessageBlocks);
    }

    public static void sendRequestEndMessage(DatabaseRequestCommandGroup.CommandType commandType,
                                             DatabaseConnectionInfo databaseConnectionInfo,
                                             RequestDTO requestDTO,
                                             String requestUUID,
                                             String executeResult) {
        List<LayoutBlock> endMessageBlocks = new ArrayList<>();
        endMessageBlocks.add(BasicBlockFactory.findHeaderBlock(":checkered_flag: Request Execution Completed !", "RequestExecuteFailed"));

        // 요청 ID
        endMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Request UUID:* `" + requestUUID + "`",
                "endblock1"));

        // Target DB 정보
        endMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Target Database:*" + databaseConnectionInfo.databaseSummary(),
                "endblock2"));

        // 요청 커맨드 종류
        endMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "endblock3"));
        endMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 내용
        List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(commandType, requestDTO);
        endMessageBlocks.addAll(contentBlocks);

        // 요청 종료 보고 내용
        endMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Execution Result:*\n ```" + executeResult + "```",
                "endblock4"));
        endMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 작업 종료 시간
        endMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Execution Finish Time:* `" + LocalDateTime.now() + "`",
                "endblock5"));

        slackService.sendBlockMessage(endMessageBlocks);
    }

    public void handleAccept(Message requestMessage, String slackUserId) {
        List<LayoutBlock> requestBlocks = requestMessage.getBlocks();
        Message.Metadata metadata = requestMessage.getMetadata();
        Map<String, Object> eventPayload = metadata.getEventPayload();

        resetAcceptDenyButtonBlock(requestBlocks, "approve", requestMessage.getTs());

        /**
         * message metadata에서 조회 key로 uuid 가져옴
         * key로 db_request 테이블에서 데이터 조회 (uuid 조건)
         * 조회해온 데이터를 아래 변수 필드에 대입
         */

        String findRequestUUID = (String) eventPayload.get(SlackConstants.MetadataKeys.messageMetadataRequestUUID);
        SlackDatabaseIntegratedDTO slackDatabaseIntegratedDTO = slackDatabaseRequestService.findSlackDatabaseRequest(findRequestUUID);
        DatabaseConnectionInfo findDatabaseConnectionInfo = slackDatabaseIntegratedDTO.getDatabaseConnectionInfo()
        DatabaseRequestCommandGroup.CommandType findCommandType = slackDatabaseIntegratedDTO.getCommandType();
        RequestDTO findRequestDTO = slackDatabaseIntegratedDTO.getRequestDTO();

        findRequestAcceptMessageBlocks(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId, findRequestUUID);

        try {
            findRequestExecuteStartMessageBlocks(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId, findRequestUUID);
            String executeResult = blockPageManager.execute(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId);
            sendRequestEndMessage(findCommandType, findDatabaseConnectionInfo, findRequestDTO, findRequestUUID, executeResult);
        } catch (Exception e) {
            findRequestFailMessageBlocks(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId, findRequestUUID, e.getMessage());
        }
    }


    private void handleDeny(Message requestMessage, String slackUserId) {
        List<LayoutBlock> requestBlocks = requestMessage.getBlocks();
        resetAcceptDenyButtonBlock(requestBlocks, "deny");

        // fetch data from message metadata
        Message.Metadata metadata = requestMessage.getMetadata();
        Map<String, Object> eventPayload = metadata.getEventPayload();

        DatabaseConnectionInfo findDatabaseConnectionInfo;
        DatabaseRequestCommandGroup.CommandType findCommandType;
        RequestDTO findRequestDTO;
        try {
            findDatabaseConnectionInfo = JsonUtil.toObject((String) eventPayload.get(SlackConstants.MetadataKeys.messageMetadataDatabaseConnectionInfo),
                    DatabaseConnectionInfo.class);
            findCommandType = JsonUtil.toObject((String) eventPayload.get(SlackConstants.MetadataKeys.messageMetadataCommandType),
                    DatabaseRequestCommandGroup.CommandType.class);
            Class findRequestDTOClassType = JsonUtil.toObject((String) eventPayload.get(SlackConstants.MetadataKeys.messageMetadataClass),
                    Class.class);
            findRequestDTO = (RequestDTO) JsonUtil.toObject((String) eventPayload.get(SlackConstants.MetadataKeys.messageMetadataRequestDTO),
                    findRequestDTOClassType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("JsonProcess 오류");
        }

//        // update slack request message (승인/반려 버튼 삭제)
//        ChatUpdateRequest request = ChatUpdateRequest.builder()
//                .channel(SlackConfig.DEFAULT_CHANNEL_ID)
//                .ts(requestMessage.getTs())
//                .blocks(requestBlocks)
//                .text("asdfawefawefasdfzxdfawef")
//                .build();
//        try {
//            ChatUpdateResponse chatUpdateResponse = slackClient.chatUpdate(request);
//            if (chatUpdateResponse.isOk()) {
//                log.info("chatUpdateResponse: {}", chatUpdateResponse);
//            } else {
//                log.error("chatUpdateResponse: {}", chatUpdateResponse);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        sendRequestDenyMessage(findCommandType, findDatabaseConnectionInfo, findRequestDTO, slackUserId);
    }

    private void sendRequestDenyMessage(DatabaseRequestCommandGroup.CommandType commandType,
                                        DatabaseConnectionInfo databaseConnectionInfo,
                                        RequestDTO requestDTO,
                                        String slackUserId) {
        List<LayoutBlock> denyMessageBlocks = new ArrayList<>();
        denyMessageBlocks.add(BasicBlockFactory.findHeaderBlock(":open_mouth: Request Denied !", "RequestDenied"));
        // Target DB 정보
        denyMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Target Database:*" + databaseConnectionInfo.databaseSummary(),
                "requestblock1"));

        // 요청 커맨드 종류
        denyMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "requestblock2"));
        denyMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 요청 내용
        List<LayoutBlock> contentBlocks = blockPageManager.handleSubmissionRequestMessage(commandType, requestDTO);
        denyMessageBlocks.addAll(contentBlocks);

        // 반려자 slack id 멘션
        denyMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Denied By:* <@" + slackUserId + ">",
                "requestblock3"));
        denyMessageBlocks.add(BasicBlockFactory.findDividerBlock());

        // 반려 시간
        denyMessageBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Denied Time:* `" + LocalDateTime.now() + "`",
                "requestblock4"));
        slackService.sendBlockMessage(denyMessageBlocks);
    }

    private void resetAcceptDenyButtonBlock(List<LayoutBlock> requestBlocks, String requestAckMessage, String ts) {
        SectionBlock requestAckBlock = BasicBlockFactory.getMarkdownTextSection("request " + requestAckMessage, SlackConstants.CommunicationBlockIds.commandRequestAcceptDenyButtonBlockId);
        int acceptDenyBlockIndex = SlackService.findBlockIndex(requestBlocks, "actions", SlackConstants.CommunicationBlockIds.commandRequestAcceptDenyButtonBlockId);
        requestBlocks.set(acceptDenyBlockIndex, requestAckBlock);

        // update slack request message (승인/반려 버튼 삭제)
        ChatUpdateRequest request = ChatUpdateRequest.builder()
                .channel(SlackConfig.DEFAULT_CHANNEL_ID)
                .ts(ts)
                .blocks(requestBlocks)
                .text("asdfawefawefasdfzxdfawef")
                .build();
        try {
            ChatUpdateResponse chatUpdateResponse = slackClient.chatUpdate(request);
            if (chatUpdateResponse.isOk()) {
                log.info("chatUpdateResponse: {}", chatUpdateResponse);
            } else {
                log.error("chatUpdateResponse: {}", chatUpdateResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
