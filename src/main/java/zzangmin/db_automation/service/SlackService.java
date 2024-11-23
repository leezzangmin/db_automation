package zzangmin.db_automation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.slack.api.model.Message;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import com.slack.api.model.block.element.*;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.config.SlackConfig;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.view.SlackConstants;
import zzangmin.db_automation.util.JsonUtil;

import javax.net.ssl.SSLHandshakeException;
import java.util.*;

import static zzangmin.db_automation.config.SlackConfig.DEFAULT_CHANNEL_ID;
import static zzangmin.db_automation.config.SlackConfig.MAX_MESSAGE_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackService {

    private final MethodsClient slackClient;


    /**
     * 특정 유저(admin)만 request 를 승인/반려 할 수 있음.
     *
     */
    public void validateRequestAcceptDoerAdmin(String slackUserId) {
        // TODO
        if (!SlackConfig.slackAdminUserIds.contains(slackUserId)) {
            throw new IllegalArgumentException("해당 user 가 처리할 수 없는 action 입니다.");
        }
    }

    public void sendNormalStringMessage(String message) {
        if (message.isBlank()) {
            return;
        }
        log.info("normal slack message: {}", message);
        for (int start = 0; start < message.length(); start += MAX_MESSAGE_SIZE) {
            int end = Math.min(message.length(), start + MAX_MESSAGE_SIZE);
            String messageChunk = message.substring(start, end);

            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(DEFAULT_CHANNEL_ID)
                    .text(messageChunk)
                    .build();
            ChatPostMessageResponse chatPostMessageResponse = null;
            try {
                chatPostMessageResponse = slackClient.chatPostMessage(request);
            } catch (SSLHandshakeException sslHandshakeException) {
                log.info(sslHandshakeException.getMessage());
                break;
            } catch (Exception e) {
                log.info(e.getMessage());
            }

            if (chatPostMessageResponse.isOk()) {
                log.info("chatPostMessageResponse: {}", chatPostMessageResponse);
            } else {
                log.error("chatPostMessageResponse: {}", chatPostMessageResponse);
            }
        }
    }

    public void sendBlockMessage(List<LayoutBlock> blocks) {
        if (blocks.isEmpty()) {
            return;
        }
        log.info("block slack message: {}", blocks);

        List<LayoutBlock> truncatedBlocks = new ArrayList<>();
        for (LayoutBlock block : blocks) {
            if (block instanceof SectionBlock) {
                SectionBlock sectionBlock = (SectionBlock) block;
                TextObject text = sectionBlock.getText();
                if (text instanceof PlainTextObject || text instanceof MarkdownTextObject) {
                    String originalText = text.getText();
                    while (originalText.length() > 3000) {
                        String partText = originalText.substring(0, 3000);
                        originalText = originalText.substring(3000);
                        if (text instanceof PlainTextObject) {
                            truncatedBlocks.add(SectionBlock.builder().text(PlainTextObject.builder().text(partText).build()).build());
                        } else {
                            truncatedBlocks.add(SectionBlock.builder().text(MarkdownTextObject.builder().text(partText).build()).build());
                        }
                    }
                    if (text instanceof PlainTextObject) {
                        truncatedBlocks.add(SectionBlock.builder().text(PlainTextObject.builder().text(originalText).build()).build());
                    } else {
                        truncatedBlocks.add(SectionBlock.builder().text(MarkdownTextObject.builder().text(originalText).build()).build());
                    }
                } else {
                    truncatedBlocks.add(block);
                }
            } else {
                truncatedBlocks.add(block);
            }
        }

        List<List<LayoutBlock>> chunkedBlocks = chunkBlocks(truncatedBlocks, 50);

        for (List<LayoutBlock> chunk : chunkedBlocks) {
            sendChunkedBlockMessage(chunk);
        }
    }

    private List<List<LayoutBlock>> chunkBlocks(List<LayoutBlock> blocks, int chunkSize) {
        List<List<LayoutBlock>> chunkedBlocks = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i += chunkSize) {
            chunkedBlocks.add(new ArrayList<>(blocks.subList(i, Math.min(blocks.size(), i + chunkSize))));
        }
        return chunkedBlocks;
    }

    private void sendChunkedBlockMessage(List<LayoutBlock> blocks) {
        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(DEFAULT_CHANNEL_ID)
                .blocks(blocks)
                .build();
        ChatPostMessageResponse chatPostMessageResponse = null;
        try {
            chatPostMessageResponse = slackClient.chatPostMessage(request);
        } catch (SSLHandshakeException sslHandshakeException) {
            log.info(sslHandshakeException.getMessage());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        if (chatPostMessageResponse != null && chatPostMessageResponse.getWarning() == null && chatPostMessageResponse.isOk()) {
            log.info("chatPostMessageResponse: {}", chatPostMessageResponse);
        } else {
            log.error("chatPostMessageResponse: {}", chatPostMessageResponse);
        }
    }

    public void sendBlockMessageWithMetadata(DatabaseConnectionInfo databaseConnectionInfo,
                                             DatabaseRequestCommandGroup.CommandType commandType,
                                             List<LayoutBlock> blocks,
                                             RequestDTO requestDTO,
                                             String requestUUID) throws JsonProcessingException {
        if (blocks.isEmpty()) {
            return;
        }
        log.info("block slack message: {}", blocks);
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(SlackConstants.MetadataKeys.messageMetadataDatabaseConnectionInfo, JsonUtil.toJson(databaseConnectionInfo));
        metadataMap.put(SlackConstants.MetadataKeys.messageMetadataClass, JsonUtil.toJson(requestDTO.getClass()));
        metadataMap.put(SlackConstants.MetadataKeys.messageMetadataRequestDTO, JsonUtil.toJson(requestDTO));
        metadataMap.put(SlackConstants.MetadataKeys.messageMetadataCommandType, JsonUtil.toJson(commandType));
        metadataMap.put(SlackConstants.MetadataKeys.messageMetadataRequestUUID, requestUUID);

        // https://api.slack.com/metadata/using
        Message.Metadata metadata = Message.Metadata.builder()
                .eventType(SlackConstants.MetadataKeys.messageMetadataMapTypeName)
                .eventPayload(metadataMap)
                .build();

        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(DEFAULT_CHANNEL_ID)
                .blocks(blocks)
                .metadata(metadata)
                .build();
        ChatPostMessageResponse chatPostMessageResponse = null;
        try {
            chatPostMessageResponse = slackClient.chatPostMessage(request);
        } catch (SSLHandshakeException sslHandshakeException) {
            log.info(sslHandshakeException.getMessage());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        if (chatPostMessageResponse != null && chatPostMessageResponse.getWarning() == null && chatPostMessageResponse.isOk()) {
            log.info("chatPostMessageResponse: {}", chatPostMessageResponse);
        } else {
            log.error("chatPostMessageResponse: {}", chatPostMessageResponse);
        }
    }

    public static int findElementIndex(List<BlockElement> blockElements, String actionId) {
        /**
         * ButtonElement
         * ChannelsSelectElement
         * CheckboxesElement
         * ConversationsSelectElement
         * DatePickerElement
         * DatetimePickerElement
         * EmailTextInputElement
         * ExternalSelectElement
         * FileInputElement
         * ImageElement
         * MultiChannelsSelectElement
         * MultiConversationsSelectElement
         * MultiExternalSelectElement
         * MultiStaticSelectElement
         * MultiUsersSelectElement
         * NumberInputElement
         * OverflowMenuElement
         * PlainTextInputElement
         * RadioButtonsElement
         * RichTextInputElement
         * RichTextListElement
         * RichTextPreformattedElement
         * RichTextQuoteElement
         * RichTextSectionElement
         * RichTextUnknownElement
         * StaticSelectElement
         * TimePickerElement
         * URLTextInputElement
         * UnknownBlockElement
         * UsersSelectElement
         * WorkflowButtonElement
         */
        for (int i = 0; i < blockElements.size(); i++) {
            BlockElement blockElement = blockElements.get(i);
            if (blockElement instanceof PlainTextInputElement) {
                PlainTextInputElement childElement = (PlainTextInputElement) blockElement;
                if (childElement.getActionId().equals(actionId)) {
                    return i;
                }
            } else if (blockElement instanceof StaticSelectElement) {
                StaticSelectElement childElement = (StaticSelectElement) blockElement;
                if (childElement.getActionId().equals(actionId)) {
                    return i;
                }
            } else if (blockElement instanceof ButtonElement) {
                ButtonElement childElement = (ButtonElement) blockElement;
                if (childElement.getActionId().equals(actionId)) {
                    return i;
                }
            } else {
                log.error("blockElement: {}", blockElement);
                throw new IllegalStateException("미지원 Element Type. 구현을 추가해야 합니다.");
            }
        }
        return -999999999;
    }

    public static int findBlockIndex(List<LayoutBlock> blocks, String blockType, String blockId) {
        for (int i = 0; i < blocks.size(); i++) {
            LayoutBlock block = blocks.get(i);
            if (block.getType().equals(blockType) && block.getBlockId().equals(blockId)) {
                return i;
            }
            if (block instanceof ActionsBlock) {
                ActionsBlock childBlock = (ActionsBlock) block;

                // 찾으려는 대상이 ActionBlock 내부 element 에 존재
                if (findElementIndex(childBlock.getElements(), blockId) != -999999999) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("해당 block 이 존재하지 않습니다.");
    }

    public static String findCurrentValueFromState(Map<String, Map<String, ViewState.Value>> values, String targetValueKey) {
        log.info("values: {}", values);
        log.info("targetValueKey: {}", targetValueKey);
        String selectedValue;
        for (String componentId : values.keySet()) {
            log.info("componentId: {}", componentId);
            Map<String, ViewState.Value> stringValueElementMap = values.get(componentId);
            for (String actionIdKey : stringValueElementMap.keySet()) {
                if (actionIdKey.equals(targetValueKey)) {
                    log.info("actionIdKey: {}", actionIdKey);
                    if (stringValueElementMap.get(actionIdKey).getSelectedOption() == null) {
                        selectedValue = stringValueElementMap.get(actionIdKey).getValue();
                        log.info("selectedValue1: {}", selectedValue);
                        if (selectedValue == null || selectedValue == "") {
                            throw new IllegalArgumentException(targetValueKey + " 이 비어있습니다.");
                        }
                        return selectedValue;
                    } else {
                        selectedValue = stringValueElementMap.get(actionIdKey)
                                .getSelectedOption()
                                .getValue();
                        log.info("selectedValue2: {}", selectedValue);
                        if (selectedValue == null || selectedValue == "") {
                            throw new IllegalArgumentException(targetValueKey + " 이 비어있습니다.");
                        }
                        return selectedValue;
                    }
                }
            }
            if (componentId.equals(targetValueKey)) {
                Map<String, ViewState.Value> stringValueMap = values.get(componentId);
                log.info("stringValueMap: {}", stringValueMap);
                ViewState.Value value = stringValueMap.get(targetValueKey);

                // static select
                if (stringValueMap.get(targetValueKey).getSelectedOption() == null) {
                    selectedValue = stringValueMap.get(targetValueKey).getValue();
                }
                // plain text input
                else {
                    selectedValue = stringValueMap.get(targetValueKey)
                            .getSelectedOption()
                            .getValue();
                }
                log.info("selectedValue3: {}", selectedValue);
                if (selectedValue == null || selectedValue == "") {
                    throw new IllegalArgumentException(targetValueKey + " 이 비어있습니다.");
                }
                return selectedValue;
            }
        }
        throw new IllegalStateException("state에 target 값이 존재하지 않습니다.");
    }

}
