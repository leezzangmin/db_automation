package zzangmin.db_automation.controller;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.slackview.*;
import zzangmin.db_automation.slackview.commandpage.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class SlackActionHandler {

    private final SelectCommandBlocks selectCommandBlocks;
    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final BlockPageManager blockPageManager;

    private final CreateIndexBlockPage createIndexBlockPage;

    public List<LayoutBlock> handleAction(BlockActionPayload.Action action, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String actionId = action.getActionId();
        blockPageManager.
        if (actionId.equals(SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId)) {
            currentBlocks = selectCommandBlocks.handleCommandGroupChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId)) {
            currentBlocks = selectCommandBlocks.handleCommandTypeChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.findClusterSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTableBlocks.handleClusterChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.CommandBlockIds.findClusterSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTableBlocks.handleSchemaChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.findTableSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTableBlocks.handleTableChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.CommandBlockIds.findTableSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.createIndexAddColumnButtonId)) {
            currentBlocks = createIndexBlockPage.handleAddColumn(currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.createIndexRemoveColumnButtonId)) {
            currentBlocks = createIndexBlockPage.handleRemoveColumn(currentBlocks, values);
        } else {
            throw new IllegalArgumentException("미지원 actionId: " + actionId);
        }
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
