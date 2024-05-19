package zzangmin.db_automation.slackview;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
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
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class SlackRequestHandler {

    private final BlockPageManager blockPageManager;

    public List<LayoutBlock> handleAction(BlockActionPayload.Action action, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String actionId = action.getActionId();
        blockPageManager.handleAction(actionId, currentBlocks, values);
        return currentBlocks;
    }

    public void handleSubmission(DatabaseRequestCommandGroup.CommandType commandType, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        validateSubmission();
        log.info("<submission> commandType: {}", commandType);
        blockPageManager.handleSubmission(commandType, currentBlocks, values);
    }

    public void validateRequest(String slackSignature, String timestamp, String requestBody) {
        try {
            String secret = SlackConfig.slackAppSigningSecret;
            String baseString = "v0:" + timestamp + ":" + requestBody;

            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(baseString.getBytes());
            String mySignature = "v0=" + Hex.encodeHexString(hash);

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
