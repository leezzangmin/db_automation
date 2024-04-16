package zzangmin.db_automation.controller;

import com.slack.api.Slack;
import com.slack.api.app_backend.interactive_components.ActionResponseSender;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.response.ActionResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.util.json.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.service.SlackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final SlackService slackService;
    private final MethodsClient slackClient;

    @PostMapping("/slack/callback")
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload) throws IOException {
        log.info("payload: {}", payload);
        BlockActionPayload blockActionPayload = GsonFactory.createSnakeCase()
                .fromJson(payload, BlockActionPayload.class);
        List<BlockActionPayload.Action> actions = blockActionPayload.getActions();
        List<LayoutBlock> blocks = blockActionPayload.getMessage().getBlocks();
        String username = blockActionPayload.getUser().getUsername();

        for (BlockActionPayload.Action action : actions) {
            if (action.getActionId().equals(slackService.findClusterSelectsElementActionId)) {
                String DBMSName = action.getSelectedOption().getValue();
                 //       StaticSelectElement clusterSelects = (StaticSelectElement) ((ActionsBlock) block).getElements().get(0);

                break;
            }
            else if (action.getActionId().equals(slackService.findSubmitButton())) {

                break;
            }
        }

        log.info("callback blockActionPayload: {}", blockActionPayload);
        ActionResponse response =
                ActionResponse.builder()
                        .replaceOriginal(true)
                        .blocks(blocks)
                        .build();
        log.info("callback response: {}", response);
        ActionResponseSender sender = new ActionResponseSender(Slack.getInstance());
        sender.send(blockActionPayload.getResponseUrl(), response);
        return ResponseEntity.ok(true);
    }


    @GetMapping("/slack/command/dbselect")
    public void sendSlackMessage(String message, String channelID) {
        String channelAddress = channelID;

        List<LayoutBlock> layoutBlocks = new ArrayList<>();
        layoutBlocks.add(slackService.getTextSection("august bot slack message test"));
        layoutBlocks.add(divider());
        layoutBlocks.add(slackService.findClusterSelectsBlock());
        layoutBlocks.add(slackService.findSchemaSelects(null));
        layoutBlocks.add(slackService.findSubmitButton());

        for (LayoutBlock layoutBlock : layoutBlocks) {
            log.info("layoutBlock: {}", layoutBlock);
        }

        try {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelAddress)
                    .text(message)
                    .blocks(layoutBlocks)
                    .build();

            ChatPostMessageResponse chatPostMessageResponse = slackClient.chatPostMessage(request);
            log.info("ChatPostMessageResponse: {}", chatPostMessageResponse);

        } catch (SlackApiException | IOException e) {
            log.error(e.getMessage());
        }
    }

}
