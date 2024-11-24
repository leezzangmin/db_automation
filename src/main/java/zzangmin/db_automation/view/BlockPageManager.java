package zzangmin.db_automation.view;

import com.slack.api.methods.request.chat.ChatUpdateRequest;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.SlackConfig;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.view.slackrequestpage.SlackRequestMessagePage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BlockPageManager {

    private final List<BlockPage> blockPages;
    private final SlackService slackService;

    @Autowired
    public BlockPageManager(@Lazy List<BlockPage> blockPages, SlackService slackService) {
        this.blockPages = blockPages;
        this.slackService = slackService;
    }

    // 커맨드타입에 해당하는 view block page 클래스를 찾아서 generate 된 view를 리턴
    public List<LayoutBlock> generateBlocks(DatabaseRequestCommandGroup.CommandType commandType) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);

        return selectedBlockPage.generateBlocks();
    }

    // 각 커맨드타입에 맞는 view modal page 클래스에서 제출 액션을 핸들링
    public RequestDTO handleSubmission(DatabaseRequestCommandGroup.CommandType commandType,
                                       Map<String, Map<String, ViewState.Value>> values) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);

        return selectedBlockPage.handleSubmission(values);
    }

    public List<LayoutBlock> handleSubmissionRequestMessage(DatabaseRequestCommandGroup.CommandType commandType,
                                                            RequestDTO requestDTO) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);

        return selectedBlockPage.generateRequestMessageBlocks(requestDTO);
    }

    // 각 클래스에서 block action 을 컨트롤할 수 있도록 라우팅
    public List<LayoutBlock> handleViewAction(String actionId, List<LayoutBlock> blocks, Map<String, Map<String, ViewState.Value>> values) {
        BlockPage selectedBlockPage = findBlockPageByActionId(actionId);
        selectedBlockPage.handleViewAction(actionId, blocks, values);
        return blocks;
    }

    public List<LayoutBlock> handleMessageAction(String actionId, String userId, Message message) {
        SlackRequestMessagePage selectedBlockPage = (SlackRequestMessagePage) findBlockPageByActionId(actionId);

        if (actionId.equals(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)) {
            selectedBlockPage.handleAccept(message, userId);
        } else if (actionId.equals(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)) {
            handleDeny(message, userId);
        } else {
            throw new IllegalArgumentException("미지원 actionId: " + actionId);
        }


        ChatUpdateRequest chatUpdateRequest = ChatUpdateRequest.builder()
                .channel(SlackConfig.DEFAULT_CHANNEL_ID)
                .ts(message.getTs())
                .blocks(message.getBlocks())
                .text("asdfawefawefasdfzxdfawef")
                .build();
        slackService.sendChatUpdateRequest(chatUpdateRequest);

        return message.getBlocks();
    }

    private BlockPage findBlockPageByCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        List<BlockPage> filteredBlockPages = this.blockPages.stream()
                .filter(h -> h.supportsCommandType(commandType))
                .toList();
        if (filteredBlockPages.size() != 1) {
            throw new IllegalArgumentException("Unsupported or ambiguous command type: " + commandType);
        }
        BlockPage selectedBlockPage = filteredBlockPages.get(0);
        return selectedBlockPage;
    }

    private BlockPage findBlockPageByActionId(String actionId) {
        List<BlockPage> filteredBlockPages = this.blockPages.stream()
                .filter(h -> h.supportsActionId(actionId))
                .toList();
        if (filteredBlockPages.size() != 1) {
            throw new IllegalArgumentException("Unsupported or ambiguous actionId: " + actionId);
        }
        BlockPage selectedBlockPage = filteredBlockPages.get(0);
        return selectedBlockPage;
    }

    // 각 view block page 클래스의 execute (실제 커맨드 실행)
    private String execute(DatabaseRequestCommandGroup.CommandType commandType,
                           DatabaseConnectionInfo databaseConnectionInfo,
                           RequestDTO requestDTO,
                           String slackUserId) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);
        String executeResult = selectedBlockPage.execute(databaseConnectionInfo, requestDTO, slackUserId);
        return executeResult;
    }
}
