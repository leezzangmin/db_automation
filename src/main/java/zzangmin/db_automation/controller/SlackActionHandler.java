package zzangmin.db_automation.controller;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class SlackActionHandler {

    private final SlackService slackService;
    private final SelectCommand selectCommand;
    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final CreateIndexBlockPage createIndexBlockPage;

    public List<LayoutBlock> handleAction(BlockActionPayload.Action action, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String actionId = action.getActionId();
        if (actionId.equals(SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId)) {
            currentBlocks = selectCommand.handleCommandGroupChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId)) {
            currentBlocks = selectCommand.handleCommandTypeChange(currentBlocks, values);


            log.info("{} currentBlocks: {}", SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.findClusterSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTable.handleClusterChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.CommandBlockIds.findClusterSelectsElementActionId, currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTable.handleSchemaChange(currentBlocks, values);
            log.info("{} currentBlocks: {}", SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId, currentBlocks);
        }
        else if (actionId.equals(SlackConstants.CommandBlockIds.findTableSelectsElementActionId)) {
            currentBlocks = selectClusterSchemaTable.handleTableChange(currentBlocks, values);
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
        if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_INDEX)) {
            createIndexBlockPage.handleSubmission(currentBlocks, values);
            log.info("currentBlocks: {}", currentBlocks);
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_TABLE)) {
            return;
        } else {
            throw new IllegalArgumentException("미지원 commandType: " + commandType);
        }
    }

    private void validateSubmission() {
        // TODO
    }

//    public List<LayoutBlock> handleException(List<LayoutBlock> currentBlocks, Exception e) {
//        // TODO: https://api.slack.com/surfaces/modals#displaying_errors
//        int contextBlockIndex = 99999999;
//        try {
//            contextBlockIndex = SlackService.findBlockIndex(currentBlocks, "context", SlackController.errorContextBlockId);
//        } catch (IllegalArgumentException notFoundIndexException) {}
//        if (contextBlockIndex == 99999999) {
//            currentBlocks.add(BasicBlockFactory.getContextBlock(e.getMessage(), SlackController.errorContextBlockId));
//            return currentBlocks;
//        }
//        currentBlocks.remove(contextBlockIndex);
//        currentBlocks.add(BasicBlockFactory.getContextBlock(e.getMessage(), SlackController.errorContextBlockId));
//        return currentBlocks;
//    }
//    private void buildErrorResponse(ViewSubmissionPayload payload, String errorDescription, HttpServletResponse response) throws IOException {
//        List<LayoutBlock> blocks = payload.getView().getBlocks();
//        if (blocks.size() == BlockId.values().length) {
//            blocks.remove(BlockId.values().length - 1);
//        }
//        blocks.add(SectionBlock.builder()
//                .blockId(ERROR.name())
//                .text(MarkdownTextObject.builder()
//                        .text(errorDescription)
//                        .build()).build());
//
//        boolean isAdmin = vacationAdminService.isAdmin(payload.getUser().getId(), payload.getUser().getTeamId());
//        View viewWithError = buildAddVacationInfoView(payload.getView().getCallbackId(), payload.getUser().getId(), isAdmin);
//        viewWithError.setBlocks(blocks);
//        ViewSubmissionResponse submissionResponse = ViewSubmissionResponse.builder()
//                .responseAction("update")
//                .view(viewWithError)
//                .build();
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write(gson.toJson(submissionResponse));
//        response.getWriter().flush();
//    }

}
