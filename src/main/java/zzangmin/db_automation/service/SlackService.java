package zzangmin.db_automation.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
import static zzangmin.db_automation.config.SlackConfig.DEFAULT_CHANNEL_ID;
import static zzangmin.db_automation.config.SlackConfig.MAX_MESSAGE_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackService {

    private final DescribeService describeService;
    private final MethodsClient slackClient;
    private final DynamicDataSourceProperties dataSourceProperties;

    public String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";
    public String findClusterSelectsElementActionId = "selectClusterName";
    public String findSchemaSelectsElementActionId = "selectSchemaName";
    public String findSubmitButtonActionId = "submitButton";
    public String findPlainTextInputActionId = "plainTextInput";
    public String dividerBlockId = "dividerId";
    public String textSectionBlockId = "TextSectionId";
    public String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";
    public String findGlobalRequestModalViewId = "globalRequestModalViewId";

    public ActionsBlock findClusterSelectsBlock() {
        String findClusterSelectsElementPlaceholder = "select cluster";
        List<OptionObject> selectOptions = describeService.findDBMSNames()
                .getDbmsNames()
                .stream()
                .map(dbmsName -> OptionObject.builder()
                        .text(plainText(dbmsName))
                        .value(dbmsName)
                        .build()
                )
                .collect(Collectors.toList());
        return actions(actions -> actions.elements(asElements(StaticSelectElement.builder()
                        .options(selectOptions)
                        .placeholder(plainText(findClusterSelectsElementPlaceholder))
                        .actionId(findClusterSelectsElementActionId)
                        .build()))
                .blockId(findClusterSelectsElementActionId));
    }

    public ActionsBlock findSchemaSelects(String DBMSName) {
        String findSchemaSelectsElementPlaceholder = "select schema";
        if (DBMSName == null) {
            return actions(actions -> actions.elements(asElements(StaticSelectElement.builder()
                            .options(generateEmptyOptionObjects())
                            .placeholder(plainText("select schema"))
                            .actionId(findSchemaSelectsElementActionId)
                            .build()))
                    .blockId(findSchemaSelectsElementActionId));
        }
        DatabaseConnectionInfo databaseConnectionInfo = dataSourceProperties.findByDbName(DBMSName);

        List<OptionObject> selectOptions = describeService.findSchemaNames(databaseConnectionInfo)
                .getSchemaNames()
                .stream()
                .map(schemaName -> OptionObject.builder()
                        .text(plainText(schemaName))
                        .value(schemaName)
                        .build()
                )
                .collect(Collectors.toList());
        return actions(actions -> actions.elements(asElements(StaticSelectElement.builder()
                        .options(selectOptions)
                        .placeholder(plainText(findSchemaSelectsElementPlaceholder))
                        .actionId(findSchemaSelectsElementActionId)
                        .build()))
                .blockId(findSchemaSelectsElementActionId));
    }

    public ActionsBlock findSubmitButton() {

        return actions(actions -> actions
                .elements(asElements(
                        button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
                                .value("sadfsdfsdf")
                                .style("primary")
                                .text(plainText("submit"))
                                .actionId(findSubmitButtonActionId)
                        ))).blockId(findSubmitButtonActionId)
        );
    }

    public InputBlock findMultilinePlainTextInput() {
        return input(input -> input
                .element(plainTextInput(pti -> pti.actionId(findPlainTextInputActionId)
                        .multiline(true)
                        .placeholder(plainText("plaintexttexttextxetextxettexttextxet"))
                ))
                .label(plainText("label123123"))
                .blockId(findPlainTextInputActionId));
    }

    /**
     * DDL - CREATE TABLE, ALTER, DROP, MODIFY COLUMN ...
     * DML - INSERT, UPDATE, DELETE
     * SELECT - 단순 조회
     * MIGRATION - table mig, database mig
     * STANDARD - parameter, schema, configs
     * METRIC - cpu, memory, hll
      */
    public ActionsBlock findDatabaseRequestCommandGroupSelects() {
        String findCommandGroupPlaceholder = "select database command group";

        List<OptionObject> selectOptions = Arrays.stream(DatabaseRequestCommandGroup.values())
                .map(group -> OptionObject.builder()
                        .text(plainText(group.name()))
                        .value(group.name())
                        .build()
                )
                .collect(Collectors.toList());
        return actions(actions -> actions.elements(asElements(StaticSelectElement.builder()
                        .options(selectOptions)
                        .placeholder(plainText(findCommandGroupPlaceholder))
                        .actionId(findDatabaseRequestCommandGroupSelectsElementActionId)
                        .build()))
                .blockId(findDatabaseRequestCommandGroupSelectsElementActionId));
    }

    public ActionsBlock findDatabaseRequestCommandTypeSelects(DatabaseRequestCommandGroup group) {
        String findCommandTypePlaceholder = "select database command type";
        List<OptionObject> selectOptions = DatabaseRequestCommandGroup.findDatabaseRequestCommandTypes(group)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        return actions(actions -> actions.elements(asElements(StaticSelectElement.builder()
                        .options(selectOptions)
                        .placeholder(plainText(findCommandTypePlaceholder))
                        .actionId(findCommandTypeSelectsElementActionId)
                        .build()))
                .blockId(findCommandTypeSelectsElementActionId));
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

    public View buildCreateTableModal() {
        return View.builder()
                .type("modal")
                .callbackId("create-table-modal")
                .title(ViewTitle.builder().type("plain_text").text("Create MySQL Table").emoji(true).build())
                .submit(ViewSubmit.builder().type("plain_text").text("Submit").emoji(true).build())
                .blocks(Arrays.asList(
                        InputBlock.builder()
                                .blockId("table_name_block")
                                .element(PlainTextInputElement.builder()
                                        .actionId("table_name_action")
                                        .multiline(false)
                                        .build())
                                .label(PlainTextObject.builder().text("Table Name").emoji(true).build())
                                .build(),
                        InputBlock.builder()
                                .blockId("columns_block")
                                .element(PlainTextInputElement.builder()
                                        .actionId("columns_action")
                                        .multiline(true)
                                        .build())
                                .label(PlainTextObject.builder().text("Columns (name type isNull isUnique isAutoIncrement comment charset collate)").emoji(true).build())
                                .build(),
                        InputBlock.builder()
                                .blockId("constraints_block")
                                .element(PlainTextInputElement.builder()
                                        .actionId("constraints_action")
                                        .multiline(true)
                                        .build())
                                .label(PlainTextObject.builder().text("Constraints (type keyName)").emoji(true).build())
                                .build(),
                        InputBlock.builder()
                                .blockId("additional_settings_block")
                                .element(PlainTextInputElement.builder()
                                        .actionId("additional_settings_action")
                                        .multiline(true)
                                        .build())
                                .label(PlainTextObject.builder().text("Additional Settings (engine charset collate tableComment)").emoji(true).build())
                                .build()
                ))
                .build();
    }

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

    public SectionBlock getTextSection(String text) {
        SectionBlock sectionBlock = section(section -> section.text(plainText(text)).blockId(textSectionBlockId));
        return sectionBlock;
    }

    public DividerBlock getDivider() {
        DividerBlock divider = divider();
        divider.setBlockId(dividerBlockId);
        return divider;
    }

    private List<OptionObject> generateEmptyOptionObjects() {
        return List.of(OptionObject.builder()
                .text(plainText("empty option objects"))
                .value("dropdown option empty...")
                .build());
    }

    public int findBlockIndex(List<LayoutBlock> blocks, String blockType, String blockId) {
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
            } else {
                throw new IllegalArgumentException("지원하지 않는 LayoutBlock 하위 클래스 입니다.");
            }

        }
        throw new IllegalArgumentException("해당 block 이 존재하지 않습니다.");
    }


}
