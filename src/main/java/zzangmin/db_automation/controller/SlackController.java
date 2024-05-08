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
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
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
import zzangmin.db_automation.slackview.SelectClusterSchemaTable;
import zzangmin.db_automation.slackview.SelectCommand;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;
import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final MethodsClient slackClient;
    private final SlackService slackService;
    private final SlackActionHandler slackActionHandler;


    public static final String tableSchemaContextId = "tableSchemaContext";
    public static final String tableSchemaTextId = "tableSchemaText";
    public static final String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";
    public static final String findClusterSelectsElementActionId = "selectClusterName";
    public static final String findTableSelectsElementActionId = "selectTableName";
    public static final String findSchemaSelectsElementActionId = "selectSchemaName";

    public static final String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";
    public static final String createIndexIndexNameTextInputId = "inputCreateIndexIndexName";
    public static final String createIndexColumnNameTextInputId = "inputCreateIndexColumnName";
    public static final String createIndexAddColumnButtonId = "createIndexAddColumnButton";
    public static final String createIndexRemoveColumnButtonId = "createIndexRemoveColumnButton";
    public static final String findIndexTypeActionId = "selectIndexType";
    public static final String errorContextBlockId = "errorContextBlock";

    @PostMapping(value = "/slack/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity slackCallBack(@RequestParam String payload,
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
                viewBlocks = slackActionHandler.handleAction(action, viewBlocks, state.getValues());
            }

        } else if (payloadType.equals("view_submission")) {
            try {
                ViewSubmissionPayload viewSubmissionPayload = GsonFactory.createSnakeCase()
                        .fromJson(decodedPayload, ViewSubmissionPayload.class);
                log.info("ViewSubmissionPayload: {}", viewSubmissionPayload);

                view = viewSubmissionPayload.getView();
                viewBlocks = view.getBlocks();
                state = view.getState();
                CommandType findCommandType = findCommandType(state);
                // TODO: USER
                slackActionHandler.handleSubmission(findCommandType, viewBlocks, state.getValues());
                return ResponseEntity.ok(true);
            } catch (Exception e) {
                viewBlocks = slackActionHandler.handleException(viewBlocks, e);
                updateView(viewBlocks, view);
                throw e;
            }
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

        List<LayoutBlock> initialBlocks = SelectCommand.selectCommandGroupAndCommandTypeBlocks();

        ViewsOpenResponse viewsOpenResponse = slackClient.viewsOpen(r -> r.triggerId(triggerId)
                .view(slackService.findGlobalRequestModalView(initialBlocks)));
        log.info("viewsOpenResponse: {}", viewsOpenResponse);

//        List<LayoutBlock> layoutBlocks = new ArrayList<>();
//        layoutBlocks.add(section(section -> section.text(markdownText("새로운 배송팁이 등록되었습니다."))));
//        layoutBlocks.add(divider());
//        List<OptionObject> optionObjects = BasicBlockFactory.generateEmptyOptionObjects();
//        StaticSelectElement clusterSelects = StaticSelectElement.builder()
//                .options(optionObjects)
//                .placeholder(plainText("testblockholder"))
//                .actionId("testactionid")
//                .build();
//
//        layoutBlocks.add(
//                actions(actions -> actions
//                        .elements(asElements(
//                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
//                                        .value("deliveryTip.getSeq().toString()")
//                                        .style("primary")
//                                        .text(plainText("ddd"))
//                                        .actionId("aaa")
//                                ),
//                                clusterSelects
//                        ))
//                )
//        );
//        layoutBlock = ActionsBlock(type=actions, elements=[
//
//                ButtonElement(type=button, text=PlainTextObject(type=plain_text, text=ddd, emoji=null), actionId=aaa, url=null, value=deliveryTip.getSeq().toString(), style=primary, confirm=null, accessibilityLabel=null),
//
//                StaticSelectElement(type=static_select, placeholder=PlainTextObject(type=plain_text, text=testblockholder, emoji=null), actionId=testactionid, options=[OptionObject(text=PlainTextObject(type=plain_text, text=empty option objects, emoji=null), value=dropdown option empty..., description=null, url=null)], optionGroups=null, initialOption=null, confirm=null, focusOnLoad=null)
//
//], blockId=null)

    }

    private String generateSlackTagUserString(String userName) {
        return "<@" + userName + ">";
    }

    private CommandType findCommandType(ViewState state) {
        String selectedCommandTypeName = SlackService.findCurrentValueFromState(state.getValues(), findCommandTypeSelectsElementActionId);
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


}
