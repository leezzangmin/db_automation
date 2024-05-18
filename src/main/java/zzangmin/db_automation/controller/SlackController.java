package zzangmin.db_automation.controller;

import com.google.gson.Gson;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.util.JsonPayloadTypeDetector;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsUpdateRequest;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.methods.response.views.ViewsUpdateResponse;
import com.slack.api.model.block.*;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import com.slack.api.util.json.GsonFactory;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.SelectCommandBlocks;
import zzangmin.db_automation.slackview.SlackConstants;

import java.io.IOException;
import java.util.*;

import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final MethodsClient slackClient;
    private final SlackService slackService;
    private final SlackActionHandler slackActionHandler;
    private final Gson gson;
    private static final JsonPayloadTypeDetector payloadTypeDetector = new JsonPayloadTypeDetector();

    @PostMapping(value = "/slack/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> slackCallBack(@RequestParam String payload,
                                                                        @RequestBody String requestBody,
                                                                        @RequestHeader("X-Slack-Signature") String slackSignature,
                                                                        @RequestHeader("X-Slack-Request-Timestamp") String timestamp,
                                                                        HttpServletResponse response) throws IOException, SlackApiException {
        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        slackService.validateRequest(slackSignature, timestamp, requestBody);
        String decodedPayload = HtmlUtils.htmlUnescape(payload);
        log.info("slackCallBack decodedPayload: {}", decodedPayload);

        // https://slack.dev/java-slack-sdk/guides/shortcuts -> under the hood
        String payloadType = payloadTypeDetector.detectType(decodedPayload);

        View view;
        ViewState state;
        List<LayoutBlock> viewBlocks;

        if (payloadType.equals("block_actions")) {
            BlockActionPayload blockActionPayload = GsonFactory.createSnakeCase()
                    .fromJson(decodedPayload, BlockActionPayload.class);
            log.info("BlockActionPayload: {}", blockActionPayload);

            view = blockActionPayload.getView();
            state = view.getState();
            viewBlocks = view.getBlocks();
            List<Action> actions = blockActionPayload.getActions();

            for (Action action : actions) {
                log.info("action: {}", action);
                viewBlocks = slackActionHandler.handleAction(action, viewBlocks, state.getValues());
            }

        } else if (payloadType.equals("view_submission")) {
            ViewSubmissionPayload viewSubmissionPayload = GsonFactory.createSnakeCase()
                    .fromJson(decodedPayload, ViewSubmissionPayload.class);
            log.info("ViewSubmissionPayload: {}", viewSubmissionPayload);

            view = viewSubmissionPayload.getView();
            viewBlocks = view.getBlocks();
            state = view.getState();
            try {

                CommandType findCommandType = findCommandType(state);
                // TODO: USER auth
                slackActionHandler.handleSubmission(findCommandType, viewBlocks, state.getValues());

                return ResponseEntity.ok(closeViewJsonString());
            } catch (Exception e) {
                log.info("Exception: {}", e.getMessage());
                log.info("Exception trace: {}", e.getStackTrace());
                e.printStackTrace();
                return ResponseEntity.ok(displayErrorViewJsonString(e, viewBlocks));
            }
        } else {
            throw new IllegalArgumentException("미지원 payload");
        }

        for (LayoutBlock viewBlock : viewBlocks) {
            log.info("viewBlock: {}", viewBlock);
        }
        updateView(viewBlocks, view);
        String json = gson.toJson(viewBlocks);
        log.info("response JSON: {}", json);

        return ResponseEntity.ok("ok");
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
                                  @RequestParam("trigger_id") String triggerId,
                                  @RequestBody String requestBody,
                                  @RequestHeader("X-Slack-Signature") String slackSignature,
                                  @RequestHeader("X-Slack-Request-Timestamp") String timestamp) throws SlackApiException, IOException {
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
        log.info("triggerId: {}", triggerId);
        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        slackService.validateRequest(slackSignature, timestamp, requestBody);

//        SlashCommandPayloadParser slashCommandPayloadParser = new SlashCommandPayloadParser();
//        SlashCommandPayload slashCommandPayload = slashCommandPayloadParser.parse(requestBody);

        List<LayoutBlock> initialBlocks = new ArrayList<>();
//        initialBlocks.add()
        initialBlocks.addAll(SelectCommandBlocks.selectCommandGroupAndCommandTypeBlocks());
        ViewsOpenResponse viewsOpenResponse = slackClient.viewsOpen(r -> r.triggerId(triggerId)
                .view(slackService.findGlobalRequestModalView(initialBlocks)));
        log.info("viewsOpenResponse: {}", viewsOpenResponse);
    }

    private String generateSlackTagUserString(String userName) {
        return "<@" + userName + ">";
    }

    private CommandType findCommandType(ViewState state) {
        String selectedCommandTypeName = SlackService.findCurrentValueFromState(state.getValues(), SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);
        CommandType findCommandType = DatabaseRequestCommandGroup.findCommandTypeByCommandTypeName(selectedCommandTypeName);
        return findCommandType;
    }

    private void updateView(List<LayoutBlock> viewBlocks, View view) throws IOException, SlackApiException {
        ViewsUpdateRequest viewsUpdateRequest = ViewsUpdateRequest.builder()
                .view(slackService.findGlobalRequestModalView(viewBlocks))
                .viewId(view.getId())
                .build();
        ViewsUpdateResponse viewsUpdateResponse = slackClient.viewsUpdate(viewsUpdateRequest);
        log.info("viewsUpdateResponse: {}", viewsUpdateResponse);
    }

    private String closeViewJsonString() {
        // TODO: https://api.slack.com/surfaces/modals#close_all_views
        String closeViewResponseJson = "{\"response_action\":\"clear\"}";
        return closeViewResponseJson;
    }

    private String displayErrorViewJsonString(Exception e, List<LayoutBlock> blocks) {
        int errorBlockIndex = SlackService.findBlockIndex(blocks, "input", SlackConstants.ErrorBlockIds.errorMessageBlockId);
        String errorBlockId = blocks.get(errorBlockIndex).getBlockId();
        String errorMessage = e.getMessage().replace("\"", "\'");
        String errorViewResponseJson = "{\"response_action\":\"errors\",\"errors\": {\"" + errorBlockId + "\":\"" + errorMessage + "\"}}";
        log.info("errorViewResponseJson: {}", errorViewResponseJson);
        return errorViewResponseJson;
    }

}
