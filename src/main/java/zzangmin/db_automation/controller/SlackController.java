package zzangmin.db_automation.controller;

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

import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.SelectClusterSchemaTable;
import zzangmin.db_automation.slackview.SelectCommand;

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
    private final DDLController ddlController;

    private final SelectClusterSchemaTable selectClusterSchemaTable;

    public static String tableSchemaLabelId = "tableSchemaLabel";
    public static String tableSchemaTextId = "tableSchemaText";
    public static String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";
    public static String findClusterSelectsElementActionId = "selectClusterName";
    public static String findTableSelectsElementActionId = "selectTableName";
    public static String findSchemaSelectsElementActionId = "selectSchemaName";

    public static String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";

    @PostMapping(value = "/slack/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload,
                                                 @RequestBody String requestBody,
                                                 @RequestHeader("X-Slack-Signature") String slackSignature,
                                                 @RequestHeader("X-Slack-Request-Timestamp") String timestamp) throws IOException, SlackApiException {
        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        slackService.validateRequest(slackSignature, timestamp, requestBody);
        String decodedPayload = HtmlUtils.htmlUnescape(payload);
        log.info("slackCallBack decodedPayload: {}", decodedPayload);

        // https://slack.dev/java-slack-sdk/guides/shortcuts under the hood
        JsonPayloadTypeDetector typeDetector = new JsonPayloadTypeDetector();
        String payloadType = typeDetector.detectType(decodedPayload);

        View view = null;
        ViewState state;
        List<LayoutBlock> viewBlocks = null;

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
                if (action.getActionId().equals(findDatabaseRequestCommandGroupSelectsElementActionId)) {
                    viewBlocks = SelectCommand.handleCommandGroupChange(viewBlocks, state.getValues());
                    log.info("{} viewBlock: {}", findDatabaseRequestCommandGroupSelectsElementActionId, viewBlocks);
                    break;
                } else if (action.getActionId().equals(findCommandTypeSelectsElementActionId)) {
                    CommandType findCommandType = findCommandType(state);

                    viewBlocks.addAll(generateCommandTypeBlocks(findCommandType));
                    log.info("{} viewBlock: {}", findCommandTypeSelectsElementActionId, viewBlocks);
                    break;
                } else if (action.getActionId().equals(findClusterSelectsElementActionId)) {
                    viewBlocks = selectClusterSchemaTable.handleClusterChange(viewBlocks, state.getValues());
                    log.info("{} viewBlock: {}", findClusterSelectsElementActionId, viewBlocks);
                    break;
                } else if (action.getActionId().equals(findSchemaSelectsElementActionId)) {
                    viewBlocks = selectClusterSchemaTable.handleSchemaChange(viewBlocks, state.getValues());
                    log.info("{} viewBlock: {}", findSchemaSelectsElementActionId, viewBlocks);
                    break;
                }
                else if (action.getActionId().equals(findTableSelectsElementActionId)) {
                    viewBlocks = selectClusterSchemaTable.handleTableChange(viewBlocks, state.getValues());
                    log.info("{} viewBlock: {}", findTableSelectsElementActionId, viewBlocks);
                    break;
                }
            }

        } else if (payloadType.equals("view_submission")) {
            ViewSubmissionPayload viewSubmissionPayload = GsonFactory.createSnakeCase()
                    .fromJson(decodedPayload, ViewSubmissionPayload.class);
            log.info("ViewSubmissionPayload: {}", viewSubmissionPayload);

            view = viewSubmissionPayload.getView();
            state = view.getState();
            CommandType findCommandType = findCommandType(state);

//            List<LayoutBlock> layoutBlocks = generateCommandTypeBlocks(findCommandType);
//            viewBlocks = layoutBlocks;


        } else {
            throw new IllegalArgumentException("미지원 payload");
        }

        for (LayoutBlock viewBlock : viewBlocks) {
            log.info("viewBlock: {}", viewBlock);
        }
        updateView(viewBlocks, view);

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

        List<LayoutBlock> blocks = SelectCommand.selectCommandGroupAndCommandTypeBlocks();

        ViewsOpenResponse viewsOpenResponse = slackClient.viewsOpen(r -> r.triggerId(triggerId)
                .view(slackService.findGlobalRequestModalView(blocks)));
        log.info("viewsOpenResponse: {}", viewsOpenResponse);

    }

    private String generateSlackTagUserString(String userName) {
        return "<@" + userName + ">";
    }

    private CommandType findCommandType(ViewState state) {
        String selectedCommandTypeName = SlackService.findCurrentValueFromState(state.getValues(), findCommandTypeSelectsElementActionId);
        CommandType findCommandType = findCommandTypeByCommandTypeName(selectedCommandTypeName);
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

    private List<LayoutBlock> generateCommandTypeBlocks(CommandType commandType) {
        if (commandType.equals(CommandType.CREATE_INDEX)) {
            return selectClusterSchemaTable.selectClusterSchemaTableBlocks();
        } else if (commandType.equals(CommandType.CREATE_TABLE)) {
            // generate createtableblock and add to blocks
        } else if (commandType.equals(CommandType.ADD_COLUMN)) {
            // generate createaddcolumnblock and add to blocks
        }
        // and so on...

        log.info("commandType blocks: {}", blocks);
        return null;
    }
}
