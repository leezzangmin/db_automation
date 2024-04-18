package zzangmin.db_automation.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

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

    public String findClusterSelectsElementActionId = "selectClusterName";
    public String findSchemaSelectsElementActionId = "selectSchemaName";

    public String findSubmitButtonActionId = "submitButton";
    public String findPlainTextInputActionId = "plainTextInput";
    public String textSectionBlockId = "TextSectionId";
    public String dividerBlockId = "dividerId";

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

    private List<LayoutBlock> updateBlock(String blockType, String actionId) {
        // ex) blockType=static_select, actionId=selectedClusterName

        return null;
    }

    public SectionBlock getTextSection(String text) {
        SectionBlock section1 = section(section -> section.text(plainText(text)).blockId(textSectionBlockId));
        return section1;
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
