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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SlackRequestHandler;
import zzangmin.db_automation.slackview.globalpage.SelectCommandBlocks;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.util.JsonUtil;

import java.io.IOException;
import java.util.*;

import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final MethodsClient slackClient;
    private final SlackRequestHandler slackRequestHandler;
    private final SlackService slackService;
    private final Gson gson;
    private static final JsonPayloadTypeDetector payloadTypeDetector = new JsonPayloadTypeDetector();

    @PostMapping(value = "/slack/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> slackCallBack(@RequestParam String payload,
                                           @RequestBody String requestBody,
                                           @RequestHeader("X-Slack-Signature") String slackSignature,
                                           @RequestHeader("X-Slack-Request-Timestamp") String timestamp) throws IOException, SlackApiException {

        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        String decodedPayload = HtmlUtils.htmlUnescape(payload);
        slackRequestHandler.validateRequest(slackSignature, timestamp, requestBody);
        log.info("slackCallBack decodedPayload: {}", decodedPayload);

        // https://slack.dev/java-slack-sdk/guides/shortcuts -> under the hood
        String payloadType = payloadTypeDetector.detectType(decodedPayload);

        List<LayoutBlock> layoutBlocks;
        View view;
        if (payloadType.equals("block_actions")) {
            BlockActionPayload blockActionPayload = GsonFactory.createSnakeCase()
                    .fromJson(payload, BlockActionPayload.class);
            log.info("BlockActionPayload: {}", blockActionPayload);
            view = blockActionPayload.getView();

            layoutBlocks = handleBlockAction(blockActionPayload);
        } else if (payloadType.equals("view_submission")) {
            ViewSubmissionPayload viewSubmissionPayload = GsonFactory.createSnakeCase()
                    .fromJson(decodedPayload, ViewSubmissionPayload.class);
            log.info("ViewSubmissionPayload: {}", viewSubmissionPayload);

            return handleViewSubmission(viewSubmissionPayload);
        } else {
            throw new IllegalArgumentException("미지원 payload");
        }

        for (LayoutBlock block : layoutBlocks) {
            log.info("block: {}", block);
        }

        updateView(layoutBlocks, view);

        return ResponseEntity.ok("ok");
    }

    private ResponseEntity<String> handleViewSubmission(ViewSubmissionPayload viewSubmissionPayload) {
        List<LayoutBlock> blocks = viewSubmissionPayload.getView().getBlocks();
        ViewState state = viewSubmissionPayload.getView().getState();
        ViewSubmissionPayload.User slackUser = viewSubmissionPayload.getUser();
        try {
            CommandType findCommandType = findCommandType(state);
            slackRequestHandler.handleSubmission(findCommandType,
                    blocks,
                    state.getValues(),
                    slackUser);
            List<LayoutBlock> requestBlocks = slackRequestHandler.sendSubmissionRequestMessage(findCommandType, slackUser);
            slackService.sendBlockMessage(requestBlocks, List.of(findCommandType, state.getValues(), slackUser));
        } catch (Exception e) {
            log.info("Exception: {}", e.getMessage());
            log.info("Exception trace: {}", e.getStackTrace());
            e.printStackTrace();
            return ResponseEntity.ok(displayErrorViewJsonString(e, blocks));
        }

        return ResponseEntity.ok(closeViewJsonString());
    }

    private List<LayoutBlock> handleBlockAction(BlockActionPayload blockActionPayload) {
        List<Action> actions = blockActionPayload.getActions();

        // view == null -> message action
        if (blockActionPayload.getView() == null) {
            User user = blockActionPayload.getUser();

            for (Action action : actions) {
                System.out.println("action = " + action);
                if (action.getActionId().equals(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)) {
                    slackRequestHandler.validateRequestAcceptDoer(user);

                    // send accept message
                    // execute

                    System.out.println("accept!");
                } else if (action.getActionId().equals(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)) {
                    slackRequestHandler.validateRequestAcceptDoer(user);

                    // send deny message
                    // expire request message

                    System.out.println("deny!");
                }

            }
            ViewState state = blockActionPayload.getState();
            List<LayoutBlock> blocks = blockActionPayload.getMessage().getBlocks();
            for (LayoutBlock block : blocks) {
                System.out.println("block = " + block);
            }
            System.out.println("state = " + state);
            //blockActionPayload.
            return blocks;
        }

        // view != null -> view modal action
        View view = blockActionPayload.getView();
        ViewState state = view.getState();
        List<LayoutBlock> blocks = view.getBlocks();
        for (Action action : actions) {
            log.info("action: {}", action);
            blocks = slackRequestHandler.handleAction(action, blocks, state.getValues());
        }
        return blocks;
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
        System.out.println("requestBody = " + requestBody);
        slackRequestHandler.validateRequest(slackSignature, timestamp, requestBody);

        List<LayoutBlock> initialBlocks = new ArrayList<>();
        initialBlocks.addAll(SelectCommandBlocks.selectCommandGroupAndCommandTypeBlocks());
        ViewsOpenResponse viewsOpenResponse = slackClient.viewsOpen(r -> r.triggerId(triggerId)
                .view(BasicBlockFactory.findGlobalRequestModalView(initialBlocks)));
        log.info("viewsOpenResponse: {}", viewsOpenResponse);
    }

    private CommandType findCommandType(ViewState state) {
        String selectedCommandTypeName = SlackService.findCurrentValueFromState(state.getValues(), SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);
        CommandType findCommandType = DatabaseRequestCommandGroup.findCommandTypeByCommandTypeName(selectedCommandTypeName);
        return findCommandType;
    }

    private void updateView(List<LayoutBlock> blocks, View view) throws IOException, SlackApiException {
        if (view == null) {
            log.info("view is null -> message action");
            return;
        }
        ViewsUpdateRequest viewsUpdateRequest = ViewsUpdateRequest.builder()
                .view(BasicBlockFactory.findGlobalRequestModalView(blocks))
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
