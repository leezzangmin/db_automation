package zzangmin.db_automation.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static zzangmin.db_automation.config.SlackConfig.DEFAULT_CHANNEL_ID;
import static zzangmin.db_automation.config.SlackConfig.MAX_MESSAGE_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackService {

    private final DescribeService describeService;
    private final MethodsClient slackClient;
    private final DynamicDataSourceProperties dataSourceProperties;

    public StaticSelectElement findClusterSelects() {
        String elementActionId = "selectedClusterName";
        String elementPlaceholder = "select cluster";

        List<OptionObject> selectOptions = describeService.findDBMSNames()
                .getDbmsNames()
                .stream()
                .map(dbmsName -> OptionObject.builder()
                        .text(plainText(dbmsName))
                        .value(dbmsName)
                        .build()
                )
                .collect(Collectors.toList());
        StaticSelectElement clusterSelects = StaticSelectElement.builder()
                .options(selectOptions)
                .placeholder(plainText(elementPlaceholder))
                .actionId(elementActionId)
                .build();

        return clusterSelects;
    }

    public StaticSelectElement findSchemaSelects(String DBMSName) {
        if (DBMSName == null) {
            return StaticSelectElement.builder()
                    .options(List.of(OptionObject.builder().build()))
                    .placeholder(plainText("select schema"))
                    .actionId("selectedSchemaName")
                    .build();
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
        StaticSelectElement schemaSelects = StaticSelectElement.builder()
                .options(selectOptions)
                .placeholder(plainText("select schema"))
                .actionId("selectedSchemaName")
                .build();

        return schemaSelects;
    }

    public StaticSelectElement findSchemaSelects() {
        List<StaticSelectElement> schemaSelects = new ArrayList<>();

        return null;
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

}
