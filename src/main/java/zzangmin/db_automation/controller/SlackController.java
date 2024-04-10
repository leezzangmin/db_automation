package zzangmin.db_automation.controller;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    @Value("${slack.token}")
    private String slackToken;

    @PostMapping("/slack/callback")
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload) {


        return ResponseEntity.ok(true);
    }

    @GetMapping("/slacktest")
    public void sendSlackMessage(String message, String channel) {

        String channelAddress = channel;
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
                                        .actionId("shortcut-test1")
                                )
                        ))
                )
        );

        try{
            MethodsClient methods = Slack.getInstance().methods(slackToken);

            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelAddress)
                    .text(message)
                    .blocks(layoutBlocks)
                    .build();

            ChatPostMessageResponse chatPostMessageResponse = methods.chatPostMessage(request);
            log.info("ChatPostMessageResponse: {}", chatPostMessageResponse);

        } catch (SlackApiException | IOException e) {
            log.error(e.getMessage());
        }
    }


}
