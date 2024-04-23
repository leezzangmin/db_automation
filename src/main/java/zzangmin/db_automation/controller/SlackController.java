package zzangmin.db_automation.controller;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.*;
import com.slack.api.app_backend.util.JsonPayloadExtractor;
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
import com.slack.api.model.view.ViewState;
import com.slack.api.util.json.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.security.SlackRequestSignatureVerifier;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.*;
import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final DescribeService describeService;
    private final DynamicDataSourceProperties dataSourceProperties;
    private final MethodsClient slackClient;
    private final SlackService slackService;
    private final SlackRequestSignatureVerifier slackRequestSignatureVerifier;

    public static String findCommandTypeSelectsElementActionId = "selectDatabaseRequestCommandType";
    public static String findClusterSelectsElementActionId = "selectClusterName";
    public static String findSchemaSelectsElementActionId = "selectSchemaName";
    public static String findSubmitButtonActionId = "submitButton";
    public static String findPlainTextInputActionId = "plainTextInput";
    public static String dividerBlockId = "dividerId";
    public static String textSectionBlockId = "TextSectionId";
    public static String findDatabaseRequestCommandGroupSelectsElementActionId = "selectDatabaseRequestCommandGroup";
    public static String findGlobalRequestModalViewId = "globalRequestModalViewId";




    @PostMapping(value = "/slack/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload,
                                                 @RequestBody String requestBody,
                                                 @RequestHeader("X-Slack-Signature") String slackSignature,
                                                 @RequestHeader("X-Slack-Request-Timestamp") String timestamp) throws IOException, SlackApiException {
        log.info("requestBody: {}", requestBody);
        log.info("slackSignature: {}", slackSignature);
        log.info("timestamp: {}", timestamp);
        slackRequestSignatureVerifier.validateRequest(slackSignature, timestamp, requestBody);
        String decodedPayload = HtmlUtils.htmlUnescape(payload);
        log.info("slackCallBack decodedPayload: {}", decodedPayload);

        // https://slack.dev/java-slack-sdk/guides/shortcuts under the hood
        JsonPayloadExtractor payloadExtractor = new JsonPayloadExtractor();
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
                if (action.getActionId().equals(findClusterSelectsElementActionId)) {
//                    String DBMSName = findCurrentValueFromState(state, findClusterSelectsElementActionId);
//                    log.info("DBMSName: {}", DBMSName);
//                    ActionsBlock schemaSelects = slackService.findSchemaSelects(DBMSName);
//                    ActionsBlock schemaSelects2 = BasicBlockFactory.findStaticSelectsBlock(findSchemaSelectsElementActionId, );
//                    log.info("schemaSelects: {}", schemaSelects);
//                    viewBlocks.set(SELECT_SCHEMA_ORDER_INDEX, schemaSelects);
                    break;
                } else if (action.getActionId().equals(findCommandTypeSelectsElementActionId)) {
                    log.info("commandType Selected");

                    break;
                    // https://api.slack.com/surfaces/modals#updating_views
                } else if (action.getActionId().equals(findDatabaseRequestCommandGroupSelectsElementActionId)) {
                    log.info("request Group Selected");
                    updateOnCommandGroupSelected(viewBlocks, state);
                    log.info("viewBlocks: {}", viewBlocks);
//                    private void updateOnCommandGroupSelected(List<LayoutBlock> viewBlocks, ViewState state) {
//                        int commandTypeBlockIndex = findBlockIndex(viewBlocks, "actions", slackService.findCommandTypeSelectsElementActionId);
//                        String selectedDatabaseRequestGroupName = findCurrentValueFromState(state, slackService.findDatabaseRequestCommandGroupSelectsElementActionId);
//                        DatabaseRequestCommandGroup selectedDatabaseRequestGroup = DatabaseRequestCommandGroup.findDatabaseRequestCommandGroupByName(selectedDatabaseRequestGroupName);
//                        viewBlocks.set(commandTypeBlockIndex, slackService.findDatabaseRequestCommandTypeSelects(selectedDatabaseRequestGroup));
//                    }
                    break;
                }
            }
        }

        else if (payloadType.equals("view_submission")) {
            ViewSubmissionPayload viewSubmissionPayload = GsonFactory.createSnakeCase()
                    .fromJson(decodedPayload, ViewSubmissionPayload.class);
            log.info("ViewSubmissionPayload: {}", viewSubmissionPayload);

            view = viewSubmissionPayload.getView();
            state = view.getState();
            String selectedCommandTypeName = findCurrentValueFromState(state, findCommandTypeSelectsElementActionId);
            CommandType findCommandType = findCommandTypeByCommandTypeName(selectedCommandTypeName);

            List<LayoutBlock> layoutBlocks = generateCommandTypeBlocks(findCommandType);
            viewBlocks = layoutBlocks;


        } else {
            throw new IllegalArgumentException("미지원 payload");
        }

        ViewsUpdateRequest viewsUpdateRequest = ViewsUpdateRequest.builder()
                .view(slackService.findGlobalRequestModalView(viewBlocks))
                .viewId(view.getId())
                .build();
        ViewsUpdateResponse viewsUpdateResponse = slackClient.viewsUpdate(viewsUpdateRequest);
        log.info("viewsUpdateResponse: {}", viewsUpdateResponse);

        return ResponseEntity.ok(true);
    }


//    private void handleBlockActionPayload(BlockActionPayload payload) {
//        List<LayoutBlock> viewBlocks = payload.getView().getBlocks();
//        List<BlockActionPayload.Action> actions = payload.getActions();
//
//        if (payload.getType().equals("block_actions")) {
//            for (BlockActionPayload.Action action : actions) {
//                if (action.getActionId().equals(findDatabaseRequestCommandGroupSelectsElementActionId)) {
//                    updateOnCommandGroupSelected(viewBlocks, state);
//                    break;
//                } else if (action.getActionId().equals(findCommandTypeSelectsElementActionId)) {
//
//                }
//            }
//        } else if (payload.getType().equals("view_submission")) {
//            log.info("view submission");
//
//        }
//    }

    private List<LayoutBlock> updateOnCommandGroupSelected(List<LayoutBlock> viewBlocks, ViewState state) {
        int commandTypeBlockIndex = findBlockIndex(viewBlocks,
                "actions",
                findCommandTypeSelectsElementActionId);
        String selectedDatabaseRequestGroupName = findCurrentValueFromState(state, findDatabaseRequestCommandGroupSelectsElementActionId);
        DatabaseRequestCommandGroup selectedDatabaseRequestGroup = findDatabaseRequestCommandGroupByName(selectedDatabaseRequestGroupName);
        List<OptionObject> commandTypeOptions = findDatabaseRequestCommandTypes(selectedDatabaseRequestGroup)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        viewBlocks.set(commandTypeBlockIndex, slackService.findDatabaseRequestCommandTypeSelects(commandTypeOptions));
        return viewBlocks;
    }

    private List<LayoutBlock> generateCommandTypeBlocks(CommandType commandType) {
        List<LayoutBlock> blocks = new ArrayList<>();
        if (commandType.equals(CommandType.CREATE_INDEX)) {
            // generate createindexblock and add to blocks
            List<OptionObject> clusterOptions = describeService.findDBMSNames()
                    .getDbmsNames()
                    .stream()
                    .map(dbmsName -> OptionObject.builder()
                            .text(plainText(dbmsName))
                            .value(dbmsName)
                            .build()
                    )
                    .collect(Collectors.toList());
            blocks.add(slackService.findClusterSelectsBlock(clusterOptions));

            List<OptionObject> emptyOptions = BasicBlockFactory.generateEmptyOptionObjects();

            blocks.add(slackService.findSchemaSelects(emptyOptions));
        } else if (commandType.equals(CommandType.CREATE_TABLE)) {
            // generate createtableblock and add to blocks
        } else if (commandType.equals(CommandType.ADD_COLUMN)) {
            // generate createaddcolumnblock and add to blocks
        }
        // and so on...

        log.info("commandType blocks: {}", blocks);
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
        slackRequestSignatureVerifier.validateRequest(slackSignature, timestamp, requestBody);

        List<LayoutBlock> blocks = new ArrayList<>();
        List<OptionObject> databaseRequestGroupOptions = Arrays.stream(values())
                .map(group -> OptionObject.builder()
                        .text(plainText(group.name()))
                        .value(group.name())
                        .build()
                )
                .collect(Collectors.toList());
        blocks.add(slackService.findDatabaseRequestCommandGroupSelects(databaseRequestGroupOptions));
        List<OptionObject> commandTypeOptions = findDatabaseRequestCommandTypes(EMPTY)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        blocks.add(slackService.findDatabaseRequestCommandTypeSelects(commandTypeOptions));

        ViewsOpenResponse viewsOpenResponse = slackClient.viewsOpen(r -> r.triggerId(triggerId)

                .view(slackService.findGlobalRequestModalView(blocks)));
        log.info("viewsOpenResponse: {}", viewsOpenResponse);

    }

    private String generateSlackTagUserString(String userName) {
        return "<@" + userName + ">";
    }

    private String findCurrentValueFromState(ViewState viewState, String targetValueKey) {
        Map<String, Map<String, ViewState.Value>> values = viewState.getValues();
        log.info("values: {}", values);
        log.info("targetValueKey: {}", targetValueKey);
        for (String componentId : values.keySet()) {
            Map<String, ViewState.Value> stringValueMap = values.get(componentId);
            String selectedValue = stringValueMap.get(targetValueKey).getSelectedOption().getValue();
            log.info("selectedValue: {}", selectedValue);
            return selectedValue;
        }
        throw new IllegalStateException("state에 target 값이 존재하지 않습니다.");
    }

    private int findBlockIndex(List<LayoutBlock> blocks, String blockType, String blockId) {
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
