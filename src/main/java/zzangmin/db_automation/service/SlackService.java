package zzangmin.db_automation.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.slackview.BasicBlockFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
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
    public ActionsBlock findDatabaseRequestCommandGroupSelects(List<OptionObject> databaseRequestGroupOptions) {
        String findCommandGroupPlaceholder = "select database command group";
        return BasicBlockFactory.findStaticSelectsBlock(findDatabaseRequestCommandGroupSelectsElementActionId,
                databaseRequestGroupOptions,
                findCommandGroupPlaceholder);
    }

    public ActionsBlock findDatabaseRequestCommandTypeSelects(List<OptionObject> commandTypeOptions) {
        String findCommandTypePlaceholder = "select database command type";
        return BasicBlockFactory.findStaticSelectsBlock(findCommandTypeSelectsElementActionId,
                commandTypeOptions,
                findCommandTypePlaceholder);
    }

    public View findGlobalRequestModalView(List<LayoutBlock> blocks) {
        return View.builder()
                .id(findGlobalRequestModalViewId)
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
            } catch (Exception e) {
                log.info(e.getMessage());
            }
            log.info("chatPostMessageResponse: {}", chatPostMessageResponse);
        }
    }







}
