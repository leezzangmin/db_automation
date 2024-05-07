package zzangmin.db_automation.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.config.SlackConfig;
import zzangmin.db_automation.slackview.BasicBlockFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLHandshakeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static zzangmin.db_automation.config.SlackConfig.DEFAULT_CHANNEL_ID;
import static zzangmin.db_automation.config.SlackConfig.MAX_MESSAGE_SIZE;
import static zzangmin.db_automation.controller.SlackController.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class SlackService {

    private final MethodsClient slackClient;

    public List<LayoutBlock> generateCreateIndexRequestBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        return blocks;

    }


    public ActionsBlock findClusterSelectsBlock(List<OptionObject> clusterOptions) {
        String findClusterSelectsElementPlaceholder = "select cluster";
        return BasicBlockFactory.findStaticSelectsBlock(findClusterSelectsElementActionId,
                clusterOptions,
                findClusterSelectsElementPlaceholder);
    }

    public ActionsBlock findSchemaSelects(List<OptionObject> schemaOptions) {
        String findSchemaSelectsElementPlaceholder = "select schema";
        return BasicBlockFactory.findStaticSelectsBlock(findSchemaSelectsElementActionId,
                schemaOptions,
                findSchemaSelectsElementPlaceholder);
    }


    /**
     * DDL - CREATE TABLE, ALTER, DROP, MODIFY COLUMN ...
     * DML - INSERT, UPDATE, DELETE
     * SELECT - 단순 조회
     * MIGRATION - table mig, database mig
     * STANDARD - parameter, schema, configs
     * METRIC - cpu, memory, hll
      */
//    public ActionsBlock findDatabaseRequestCommandGroupSelects(List<OptionObject> databaseRequestGroupOptions) {
//        String findCommandGroupPlaceholder = "select database command group";
//        return BasicBlockFactory.findStaticSelectsBlock(findDatabaseRequestCommandGroupSelectsElementActionId,
//                databaseRequestGroupOptions,
//                findCommandGroupPlaceholder);
//    }

//    public ActionsBlock findDatabaseRequestCommandTypeSelects(List<OptionObject> commandTypeOptions) {
//        String findCommandTypePlaceholder = "select database command type";
//        return BasicBlockFactory.findStaticSelectsBlock(findCommandTypeSelectsElementActionId,
//                commandTypeOptions,
//                findCommandTypePlaceholder);
//    }

    public View findGlobalRequestModalView(List<LayoutBlock> blocks) {
        return View.builder()
                .type("modal")
                .callbackId("global-request-modal")
                .title(ViewTitle.builder()
                        .type("plain_text")
                        .text("Database Request")
                        .emoji(true)
                        .build())
                .blocks(blocks)
                .submit(ViewSubmit.builder().type("plain_text").text("Database Request submit").emoji(true).build())
                .build();
    }

//    public View buildCreateTableModal() {
//        return View.builder()
//                .type("modal")
//                .callbackId("create-table-modal")
//                .title(ViewTitle.builder().type("plain_text").text("Create MySQL Table").emoji(true).build())
//                .submit(ViewSubmit.builder().type("plain_text").text("Submit").emoji(true).build())
//                .blocks(Arrays.asList(
//                        InputBlock.builder()
//                                .blockId("table_name_block")
//                                .element(PlainTextInputElement.builder()
//                                        .actionId("table_name_action")
//                                        .multiline(false)
//                                        .build())
//                                .label(PlainTextObject.builder().text("Table Name").emoji(true).build())
//                                .build(),
//                        InputBlock.builder()
//                                .blockId("columns_block")
//                                .element(PlainTextInputElement.builder()
//                                        .actionId("columns_action")
//                                        .multiline(true)
//                                        .build())
//                                .label(PlainTextObject.builder().text("Columns (name type isNull isUnique isAutoIncrement comment charset collate)").emoji(true).build())
//                                .build(),
//                        InputBlock.builder()
//                                .blockId("constraints_block")
//                                .element(PlainTextInputElement.builder()
//                                        .actionId("constraints_action")
//                                        .multiline(true)
//                                        .build())
//                                .label(PlainTextObject.builder().text("Constraints (type keyName)").emoji(true).build())
//                                .build(),
//                        InputBlock.builder()
//                                .blockId("additional_settings_block")
//                                .element(PlainTextInputElement.builder()
//                                        .actionId("additional_settings_action")
//                                        .multiline(true)
//                                        .build())
//                                .label(PlainTextObject.builder().text("Additional Settings (engine charset collate tableComment)").emoji(true).build())
//                                .build()
//                ))
//                .build();
//    }

    public void sendMessage(String message) {
        if (message.isBlank()) {
            return;
        }
        log.info("slack message: {}", message);
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
            }
            catch (Exception e) {
                log.info(e.getMessage());
            }
            log.info("chatPostMessageResponse: {}", chatPostMessageResponse);
        }
    }

    public static int findBlockIndex(List<LayoutBlock> blocks, String blockType, String blockId) {
        for (int i = 0; i < blocks.size(); i++) {
            LayoutBlock block = blocks.get(i);

            if (block instanceof ActionsBlock) {
                ActionsBlock childBlock = (ActionsBlock) block;
                if (childBlock.getType().equals(blockType) && childBlock.getBlockId().equals(blockId)) {
                    return i;
                }
            } else if (block instanceof SectionBlock) {
                SectionBlock childBlock = (SectionBlock) block;
                if (childBlock.getType().equals(blockType) && childBlock.getBlockId().equals(blockId)) {
                    return i;
                }
            } else if (block instanceof DividerBlock) {
                DividerBlock childBlock = (DividerBlock) block;
                if (childBlock.getType().equals(blockType) && childBlock.getBlockId().equals(blockId)) {
                    return i;
                }
            } else if (block instanceof InputBlock) {
                InputBlock childBlock = (InputBlock) block;
                if (childBlock.getType().equals(blockType) && childBlock.getBlockId().equals(blockId)) {
                    return i;
                }
            } else if (block instanceof ContextBlock) {
                ContextBlock childBlock = (ContextBlock) block;
                if (childBlock.getType().equals(blockType) && childBlock.getBlockId().equals(blockId)) {
                    return i;
                }
            } else {
                throw new IllegalArgumentException("지원하지 않는 LayoutBlock 하위 클래스 입니다.");
            }
        }
        throw new IllegalArgumentException("해당 block 이 존재하지 않습니다.");
    }

    public static String findCurrentValueFromState(Map<String, Map<String, ViewState.Value>> values, String targetValueKey) {
        log.info("values: {}", values);
        log.info("targetValueKey: {}", targetValueKey);
        String selectedValue;
        for (String componentId : values.keySet()) {
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
                log.info("selectedValue: {}", selectedValue);
                return selectedValue;
            }
        }
        throw new IllegalStateException("state에 target 값이 존재하지 않습니다.");
    }

    public void validateRequest(String slackSignature, String timestamp, String requestBody) {
        try {
            String secret = SlackConfig.slackAppSigningSecret;
            String baseString = "v0:" + timestamp + ":" + requestBody;

            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(baseString.getBytes());
            String mySignature = "v0=" + Hex.encodeHexString(hash);

            if (!mySignature.equals(slackSignature)) {
                throw new IllegalArgumentException("http 요청 검증 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("http 요청 검증 실패");
        }
    }



}
