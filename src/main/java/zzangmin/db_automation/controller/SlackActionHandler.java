package zzangmin.db_automation.controller;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.CreateIndexBlockPage;
import zzangmin.db_automation.slackview.SelectClusterSchemaTable;
import zzangmin.db_automation.slackview.SelectCommand;

import java.util.List;
import java.util.Map;

import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.findCommandTypeByCommandTypeName;

@Slf4j
@RequiredArgsConstructor
@Component
public class SlackActionHandler {

    private final SelectCommand selectCommand;
    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final CreateIndexBlockPage createIndexBlockPage;

    public List<LayoutBlock> handleAction(BlockActionPayload.Action action, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String actionId = action.getActionId();
        if (actionId.equals(SlackController.findDatabaseRequestCommandGroupSelectsElementActionId)) {
            currentBlocks = selectCommand.handleCommandGroupChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackController.findDatabaseRequestCommandGroupSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackController.findCommandTypeSelectsElementActionId)) {
            currentBlocks = selectCommand.handleCommandTypeChange(currentBlocks, values);


            log.info("{} currentBlocks: {}", SlackController.findCommandTypeSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackController.findClusterSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTable.handleClusterChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackController.findClusterSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackController.findSchemaSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTable.handleSchemaChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackController.findSchemaSelectsElementActionId, currentBlocks);
        }
        else if (actionId.equals(SlackController.findTableSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTable.handleTableChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackController.findTableSelectsElementActionId, currentBlocks);
        }


        else {
            throw new IllegalArgumentException("미지원 actionId: " + actionId);
        }
        return currentBlocks;
    }

    public void handleSubmission(DatabaseRequestCommandGroup.CommandType commandType, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        log.info("<submission> commandType: {}", commandType);
        if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_INDEX)) {
            createIndexBlockPage.handleSubmission(currentBlocks, values);
            log.info("currentBlocks: {}", currentBlocks);
        } else if(commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_TABLE)) {
            return;
        }
        else {
            throw new IllegalArgumentException("미지원 commandType: " + commandType);
        }

    }


}
