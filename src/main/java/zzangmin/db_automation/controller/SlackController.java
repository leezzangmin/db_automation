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
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.config.SlackConfig;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.request.SlackDatabaseIntegratedDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.entity.SlackDatabaseRequest;
import zzangmin.db_automation.service.SlackDatabaseRequestService;
import zzangmin.db_automation.service.SlackMessageService;
import zzangmin.db_automation.service.SlackUserService;
import zzangmin.db_automation.view.BasicBlockFactory;
import zzangmin.db_automation.view.BlockPageManager;
import zzangmin.db_automation.view.globalpage.SelectCommandBlocks;
import zzangmin.db_automation.view.SlackConstants;
import zzangmin.db_automation.view.slackrequestpage.SlackRequestMessagePage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final MethodsClient slackClient;
    private final SlackDatabaseRequestService slackDatabaseRequestService;
    private final BlockPageManager blockPageManager;
    private final SlackMessageService slackMessageService;
    private final SlackUserService slackUserService;

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
        validateRequestAuth(slackSignature, timestamp, requestBody);
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

        validateRequestAuth(slackSignature, timestamp, requestBody);

        List<LayoutBlock> initialBlocks = SelectCommandBlocks.selectCommandGroupAndCommandTypeBlocks();

        ViewsOpenResponse viewsOpenResponse = slackClient.viewsOpen(r -> r.triggerId(triggerId)
                .view(BasicBlockFactory.findGlobalRequestModalView(initialBlocks)));
        log.info("viewsOpenResponse: {}", viewsOpenResponse);
    }

    private ResponseEntity<String> handleViewSubmission(ViewSubmissionPayload viewSubmissionPayload) {
        String requestUUID = UUID.randomUUID().toString();
        List<LayoutBlock> blocks = viewSubmissionPayload.getView().getBlocks();
        ViewState state = viewSubmissionPayload.getView().getState();
        ViewSubmissionPayload.User slackUser = viewSubmissionPayload.getUser();
        try {
            CommandType findCommandType = findCommandType(state);
            String selectedDBMSName = SlackMessageService.findCurrentValueFromState(state.getValues(), SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId);
            DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbIdentifier(selectedDBMSName);
            RequestDTO requestDTO = blockPageManager.handleSubmission(findCommandType,
                    state.getValues());
            List<LayoutBlock> requestMessageBlocks = SlackRequestMessagePage.findSubmissionRequestMessageBlocks(selectedDatabaseConnectionInfo,
                    findCommandType,
                    slackUser.getId(),
                    requestUUID,
                    blockPageManager.handleSubmissionRequestMessage(findCommandType, requestDTO));

            SlackDatabaseRequest slackDatabaseRequest = slackDatabaseRequestService.saveSlackDatabaseRequest(new SlackDatabaseIntegratedDTO(selectedDatabaseConnectionInfo,
                    slackUser.getId(),
                    findCommandType,
                    requestDTO.getClass().toString(),
                    requestDTO,
                    requestUUID,
                    requestDTO.extractCommandContent(),
                    "sample",
                    LocalDateTime.now())); // TODO: modal 에서 실행시간 입력 받아야함


            slackMessageService.sendBlockMessageWithMetadata(selectedDatabaseConnectionInfo, findCommandType, requestMessageBlocks, requestDTO, requestUUID);
        } catch (Exception e) {
            log.info("Exception: {}", e.getMessage());
            log.info("Exception trace: {}", e.getStackTrace());
            return ResponseEntity.ok(displayErrorViewJsonString(e, blocks));
        }

        return ResponseEntity.ok(closeViewJsonString());
    }

    private List<LayoutBlock> handleBlockAction(BlockActionPayload blockActionPayload) {
        List<Action> actions = blockActionPayload.getActions();

        // message action
        if (blockActionPayload.getView() == null) {
            User user = blockActionPayload.getUser();
            List<LayoutBlock> blocks = new ArrayList<>();
            for (Action action : actions) {
                log.info("action: {}", action);
                slackUserService.validateRequestAcceptDoerAdmin(user.getId());
                blocks = blockPageManager.handleMessageAction(action.getActionId(), user.getId(), blockActionPayload.getMessage());
            }
            return blocks;
        }

        // view modal action
        View view = blockActionPayload.getView();
        ViewState state = view.getState();
        Map<String, Map<String, ViewState.Value>> values = state.getValues();
        List<LayoutBlock> blocks = view.getBlocks();
        for (Action action : actions) {
            log.info("action: {}", action);
            blocks = blockPageManager.handleViewAction(action.getActionId(), blocks, state.getValues());
        }
        return blocks;
    }

    private void validateRequestAuth(String slackSignature, String timestamp, String requestBody) {
        SecretKeySpec secretKey = new SecretKeySpec(SlackConfig.slackAppSigningSecret.getBytes(), "HmacSHA256");
        if (requestBody.contains(SlackConfig.verificationToken)) {
            return;
        }

        try {
            StringBuilder baseString = new StringBuilder();
            baseString.append("v0:");
            baseString.append(timestamp);
            baseString.append(":");
            baseString.append(requestBody);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(baseString.toString().getBytes());

            String mySignature = "v0=" + Hex.encodeHexString(hash);
            if (!mySignature.equals(slackSignature)) {
                throw new IllegalArgumentException("http 요청 검증 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("http 요청 검증 실패");
        }
    }

    private CommandType findCommandType(ViewState state) {
        String selectedCommandTypeName = SlackMessageService.findCurrentValueFromState(state.getValues(), SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);
        return DatabaseRequestCommandGroup.findCommandTypeByCommandTypeName(selectedCommandTypeName);
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
        int errorBlockIndex = SlackMessageService.findBlockIndex(blocks, "input", SlackConstants.ErrorBlockIds.errorMessageBlockId);
        String errorBlockId = blocks.get(errorBlockIndex).getBlockId();
        String errorMessage = e.getMessage().replace("\"", "\'");
        String errorViewResponseJson = "{\"response_action\":\"errors\",\"errors\": {\"" + errorBlockId + "\":\"" + errorMessage + "\"}}";
        log.info("errorViewResponseJson: {}", errorViewResponseJson);
        return errorViewResponseJson;
    }

}
