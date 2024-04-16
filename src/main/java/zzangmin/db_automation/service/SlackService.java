package zzangmin.db_automation.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static zzangmin.db_automation.config.SlackConfig.DEFAULT_CHANNEL_ID;
import static zzangmin.db_automation.config.SlackConfig.MAX_MESSAGE_SIZE;
import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackService {

    private final DescribeService describeService;
    private final MethodsClient slackClient;
    private final DynamicDataSourceProperties dataSourceProperties;

    public String findClusterSelectsElementActionId = "selectedClusterName";
    public String findSchemaSelectsElementActionId = "selectedSchemaName";

    public String findSubmitButtonActionId = "submitButton";

    public LayoutBlock findClusterSelectsBlock() {
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
                .placeholder(plainText(findClusterSelectsElementActionId))
                .actionId(findClusterSelectsElementPlaceholder)
                .build()))
                .blockId(findClusterSelectsElementActionId));
    }

    public LayoutBlock findSchemaSelects(String DBMSName) {
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
                .placeholder(plainText("select schema"))
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
            log.info("chatPostMessageResponse: {]", chatPostMessageResponse);
        }
    }

    private List<LayoutBlock> updateBlock(String blockType, String actionId) {
        // ex) blockType=static_select, actionId=selectedClusterName

        return null;
    }

    public LayoutBlock getTextSection(String text) {
        SectionBlock section1 = section(section -> section.text(plainText(text)));
        return section1;
    }

    private List<OptionObject> generateEmptyOptionObjects() {
        return List.of(OptionObject.builder()
                .text(plainText("empty option objects"))
                .value("dropdown option empty...")
                .build());
    }

    public int findBlockIndex(List<LayoutBlock> blocks, String blockType, String blockId) {

        for (int i = 0; i < blocks.size(); i++) {
            Class<? extends LayoutBlock> aClass = blocks.get(0).getClass();
            log.info("aclass: {}", aClass);
            System.out.println("aClass = " + aClass);
//            if (blocks.get(i).getType().equals(blockType) && blocks.get(i).getBlockId().equals(blockId)) {
//                return i;
//            }
        }
        return 1;
        //throw new IllegalArgumentException("해당 block 이 존재하지 않습니다.");
    }

}
