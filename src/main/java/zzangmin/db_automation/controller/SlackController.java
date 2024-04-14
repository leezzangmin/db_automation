package zzangmin.db_automation.controller;

import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.app_backend.interactive_components.ActionResponseSender;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.response.ActionResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.chat.ChatUpdateRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.admin.App;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.ExternalSelectElement;
import com.slack.api.util.json.GsonFactory;
import com.slack.api.webhook.WebhookResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import zzangmin.db_automation.service.SlackService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    private final SlackService slackService;
    private final MethodsClient slackClient;

    @PostMapping("/slack/callback")
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload) {

        log.info("callbaack");
        return ResponseEntity.ok(true);
    }

    @PostMapping(value= "/slack/callback2", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ActionResponse slackCallBack2(@RequestParam String payload) throws IOException {
        System.out.println("payload = " + payload);
        BlockActionPayload blockActionPayload =
                GsonFactory.createSnakeCase()
                        .fromJson(payload, BlockActionPayload.class);
        log.info("asdfasdf callback2 ");

        ActionResponse response =
                ActionResponse.builder()
                        .replaceOriginal(true)
                        .blocks(blockActionPayload.getMessage().getBlocks())
                        .build();

        Slack slack = Slack.getInstance();
        ActionResponseSender sender = new ActionResponseSender(slack);
        sender.send(blockActionPayload.getResponseUrl(), response);
        return response;
    }

    @GetMapping("/slacktest")
    public void sendSlackMessage(String message, String channelID) {
        String channelAddress = channelID;
        List<LayoutBlock> layoutBlocks = new ArrayList<>();
        // 텍스트를 남길 SectionBlock 입니다.
        layoutBlocks.add(section(section -> section.text(markdownText("새로운 배송팁이 등록되었습니다."))));
        // Action과 텍스트를 구분하기 위한 Divider 입니다.
        layoutBlocks.add(divider());
        // ActionBlock에 승인 버튼과 거부 버튼을 추가 하였습니다.
        layoutBlocks.add(
                actions(actions -> actions
                        .elements(asElements(
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
                                        .value("deliveryTip.getSeq().toString()")
                                        .style("primary")
                                        .text(plainText("ddd"))
                                        .actionId("aaa")
                                ),
                                slackService.findClusterSelects()
                        ))
                )
        );

        try{
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

    @RequestMapping(value = "/.well-known/acme-challenge/Tw3VP1zrN6oKFej-VuPfy7T0__RFu4pj8siOzSX3QMM.m1i5fTvCNv1ZzJq40C9WZFf8I4Eemo-brzMP7uPGrW0")
    public String applyHttps(HttpServletResponse response) {
        return "Tw3VP1zrN6oKFej-VuPfy7T0__RFu4pj8siOzSX3QMM.m1i5fTvCNv1ZzJq40C9WZFf8I4Eemo-brzMP7uPGrW0";
    }
//    public void updateChat() throws IOException, SlackApiException {
//        BlockActionPayload blockActionPayload = GsonFactory.createSnakeCase().fromJson("asdf", BlockActionPayload.class);
//        List<BlockActionPayload.Action> actions = blockActionPayload.getActions();
//        ActionResponse actionResponse = ActionResponse.builder()
//                .replaceOriginal(true)
//                .blocks(List.of())
//                .build();
//        MethodsClient methods = Slack.getInstance().methods(slackToken);
//        SlackConfig slackConfig = new SlackConfig();
//        slackConfig.
//        // https://api.slack.com/methods/chat.update
//        ChatUpdateRequest chatUpdateRequest = ChatUpdateRequest.builder()
//                .blocks()
//                .channel()
//                .text()
//                .ts(Timestamp.from(Instant.now()))
//                .build();
//        ChatUpdateResponse chatUpdateResponse = methods.chatUpdate(chatUpdateRequest);
//
//        ActionResponseSender actionResponseSender = new ActionResponseSender(Slack.getInstance().methods(slackToken));
//        WebhookResponse send = actionResponseSender.send(blockActionPayload.getResponseUrl(), actionResponse);
//
//    }


}
