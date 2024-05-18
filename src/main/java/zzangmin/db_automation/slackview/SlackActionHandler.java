package zzangmin.db_automation.slackview;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class SlackActionHandler {

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

    private void validateSubmission() {
        // TODO
    }

}
