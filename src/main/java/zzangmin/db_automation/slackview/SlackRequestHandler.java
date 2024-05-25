package zzangmin.db_automation.slackview;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.SlackConfig;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;


@Slf4j
@RequiredArgsConstructor
@Component
public class SlackRequestHandler {

    private final BlockPageManager blockPageManager;
    private SecretKeySpec secretKey = new SecretKeySpec(SlackConfig.slackAppSigningSecret.getBytes(), "HmacSHA256");

    public List<LayoutBlock> handleAction(BlockActionPayload.Action action, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String actionId = action.getActionId();
        blockPageManager.handleAction(actionId, currentBlocks, values);
        return currentBlocks;
    }

    public void handleSubmission(DatabaseRequestCommandGroup.CommandType commandType,
                                 List<LayoutBlock> currentBlocks,
                                 Map<String, Map<String,ViewState.Value>> values,
                                 ViewSubmissionPayload.User slackUser) {
        validateSubmission();
        log.info("<submission> commandType: {}", commandType);
        blockPageManager.handleSubmission(commandType, currentBlocks, values, slackUser);
    }

    public List<LayoutBlock> sendSubmissionRequestMessage(DatabaseRequestCommandGroup.CommandType commandType,
                                                          ViewSubmissionPayload.User slackUser) {
        List<LayoutBlock> requestBlocks = new ArrayList<>();
        String text = ":rocket: Database Request !";

        requestBlocks.add(BasicBlockFactory.findHeaderBlock(text, "test"));
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Command Type:*" + commandType.toString(),
                "asdf"));
        requestBlocks.add(BasicBlockFactory.getMarkdownTextSection("*Doer:* <@" + slackUser.getId() + ">",
                "asdf2"));

        requestBlocks.add(BasicBlockFactory.findDividerBlock());
        requestBlocks.add(
                actions(actions -> actions
                        .elements(asElements(
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
                                        .value(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)
                                        .style("primary")
                                        .actionId(SlackConstants.CommunicationBlockIds.commandRequestAcceptButtonBlockId)
                                ),
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("반려")))
                                        .value(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)
                                        .style("danger")
                                        .actionId(SlackConstants.CommunicationBlockIds.commandRequestDenyButtonBlockId)
                                )))
                        .blockId(SlackConstants.CommunicationBlockIds.commandRequestAcceptDenyButtonBlockId)));

        // commandType
        // target database
        // requestDTO content
        // request Time
        // execute Time
        // request doer
        // accept, decline button
        return requestBlocks;
    }

    /**
     * 특정 유저만 request 를 승인/반려 할 수 있음.
     *
      */

    public void validateRequestAcceptDoer(BlockActionPayload.User user) {
        if (!SlackConfig.slackAdminUserIds.contains(user.getId())) {
            throw new IllegalArgumentException("해당 user 가 처리할 수 없는 action 입니다.");
        }
    }

    public void validateRequest(String slackSignature, String timestamp, String requestBody) {
        if (requestBody.contains(SlackConfig.verificationToken)) {
            return;
        }

        try {
            StringBuilder baseString = new StringBuilder();
            baseString.append("v0:");
            baseString.append(timestamp);
            baseString.append(":");
            baseString.append(requestBody);

            log.info("baseString: {}", baseString);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(baseString.toString().getBytes());

            String mySignature = "v0=" + Hex.encodeHexString(hash);
            log.info("mySignature: {}", mySignature);
            log.info("slackSignature: {}", slackSignature);
            if (!mySignature.equals(slackSignature)) {
                throw new IllegalArgumentException("http 요청 검증 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("http 요청 검증 실패");
        }
    }

    private void validateSubmission() {
        // TODO
    }

}
