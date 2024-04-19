package zzangmin.db_automation.controller;

import com.slack.api.Slack;
import com.slack.api.app_backend.interactive_components.ActionResponseSender;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.response.ActionResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import com.slack.api.util.json.GsonFactory;
import com.slack.api.webhook.WebhookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import zzangmin.db_automation.security.SlackRequestSignatureVerifier;
import zzangmin.db_automation.service.SlackService;

import java.io.IOException;
import java.util.*;

import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final SlackService slackService;
    private final MethodsClient slackClient;
    private final SlackRequestSignatureVerifier slackRequestSignatureVerifier;

    private static final int NOTIFICATION_TEXT_MESSAGE_ORDER_INDEX = 0;
    private static final int DIVIDER_BLOCK_ORDER_INDEX = 1;
    private static final int SELECT_CLUSTER_ORDER_INDEX = 2;
    private static final int SELECT_SCHEMA_ORDER_INDEX = 3;
    private static final int SUBMIT_BUTTON_ORDER_INDEX = 4;
    private static final int TEXT_INPUT_ORDER_INDEX = 5;


    @PostMapping(value = "/slack/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload,
                                                 @RequestBody String requestBody,
                                                 @RequestHeader("X-Slack-Signature") String slackSignature,
                                                 @RequestHeader("X-Slack-Request-Timestamp") String timestamp) throws IOException {
        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        slackRequestSignatureVerifier.validateRequest(slackSignature, timestamp, requestBody);


        String decodedPayload = HtmlUtils.htmlUnescape(payload);
        log.info("slackCallBack payload: {}", payload);
        BlockActionPayload blockActionPayload = GsonFactory.createSnakeCase()
                .fromJson(decodedPayload, BlockActionPayload.class);
        log.info("blockActionPayload: {}", blockActionPayload);
        List<Action> actions = blockActionPayload.getActions();
        List<LayoutBlock> blocks = blockActionPayload.getMessage().getBlocks();
        ViewState state = blockActionPayload.getState();
        Map<String, Map<String, ViewState.Value>> values = state.getValues();

        String userId = blockActionPayload.getUser().getId();
        log.info("userId: {}", userId);

        for (Action action : actions) {
            log.info("action: {}", action);
            if (action.getActionId().equals(slackService.findClusterSelectsElementActionId)) {
                String DBMSName = findCurrentValueFromState(values, slackService.findClusterSelectsElementActionId);
                log.info("DBMSName: {}", DBMSName);
                ActionsBlock schemaSelects = slackService.findSchemaSelects(DBMSName);
                log.info("schemaSelects: {}", schemaSelects);
                blocks.set(SELECT_SCHEMA_ORDER_INDEX, schemaSelects);
                break;
            }
            else if (action.getActionId().equals(slackService.findSubmitButtonActionId)) {
                log.info("submit clicked");
                break;
            }
        }

        ActionResponse response = ActionResponse.builder()
                .replaceOriginal(true)
                .blocks(blocks)
                .build();
        log.info("callback response: {}", response);

        ActionResponseSender sender = new ActionResponseSender(Slack.getInstance());
        WebhookResponse webhookResponse = sender.send(blockActionPayload.getResponseUrl(), response);

        log.info("webhookResponse: {}", webhookResponse);
        return ResponseEntity.ok(true);
    }

    @PostMapping(value = "/slack/command/dbselect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void databaseRequestCommand(@RequestParam("token") String token,
                                  @RequestParam("team_id") String teamId,
                                  @RequestParam("team_domain") String teamDomain,
                                  @RequestParam("channel_id") String channelId,
                                  @RequestParam("channel_name") String channelName,
                                  @RequestParam("user_id") String userId,
                                  @RequestParam("user_name") String userName,
                                  @RequestParam("command") String command,
                                  @RequestParam("text") String text,
                                  @RequestParam("response_url") String responseUrl,
                                  @RequestBody String requestBody,
                                  @RequestHeader("X-Slack-Signature") String slackSignature,
                                  @RequestHeader("X-Slack-Request-Timestamp") String timestamp) {
        log.info("token: {}", token);
        log.info("teamId: {}", teamId);
        log.info("teamDomain: {}", teamDomain);
        log.info("channelId: {}", channelId);
        log.info("channelName: {}", channelName);
        log.info("userId: {}", userId);
        log.info("userName: {}", userName);
        log.info("command: {}", command);
        log.info("text: {}", text);
        log.info("responseUrl: {}", responseUrl);
        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        slackRequestSignatureVerifier.validateRequest(slackSignature, timestamp, requestBody);

        List<LayoutBlock> layoutBlocks = new ArrayList<>();
        layoutBlocks.add(NOTIFICATION_TEXT_MESSAGE_ORDER_INDEX, slackService.getTextSection(generateSlackTagUserString(userName) + " bot slack message test"));
        layoutBlocks.add(DIVIDER_BLOCK_ORDER_INDEX, slackService.getDivider());
        layoutBlocks.add(SELECT_CLUSTER_ORDER_INDEX, slackService.findClusterSelectsBlock());
        layoutBlocks.add(SELECT_SCHEMA_ORDER_INDEX, slackService.findSchemaSelects(null));
        layoutBlocks.add(SUBMIT_BUTTON_ORDER_INDEX, slackService.findSubmitButton());
        layoutBlocks.add(TEXT_INPUT_ORDER_INDEX, slackService.findMultilinePlainTextInput());

        for (LayoutBlock layoutBlock : layoutBlocks) {
            log.info("layoutBlock: {}", layoutBlock);
        }

        try {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelId)
                    .text("database request message")
                    .blocks(layoutBlocks)
                    .build();

            ChatPostMessageResponse chatPostMessageResponse = slackClient.chatPostMessage(request);
            log.info("ChatPostMessageResponse: {}", chatPostMessageResponse);

        } catch (SlackApiException | IOException e) {
            log.error(e.getMessage());
        }
    }

    private String generateSlackTagUserString(String userName) {
        return "<@" + userName + ">";
    }

    private String findCurrentValueFromState(Map<String, Map<String, ViewState.Value>> values, String targetValueKey) {
        log.info("values: {}", values);
        log.info("targetValueKey: {}", targetValueKey);
        for (String componentId : values.keySet()) {
            Map<String, ViewState.Value> stringValueMap = values.get(componentId);
            return stringValueMap.get(targetValueKey).getSelectedOption().getValue();
        }
        throw new IllegalStateException("state에 target 값이 존재하지 않습니다.");
    }

}
