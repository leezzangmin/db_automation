package zzangmin.db_automation.slackview.commandpage;

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RenameTableRequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RenameTableBlockPage implements BlockPage {

    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String newTableNameLabel = "input new table name";
    private static final String newTableNamePlaceholder = "new_table_name";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterSchemaTableBlocks());

        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.RenameTable.renameTableNewTableNameTextInputId,
                newTableNameLabel,
                newTableNamePlaceholder));

        return blocks;
    }

    @Override
    public void handleSubmission(List<LayoutBlock> currentBlocks,
                                 Map<String, Map<String, ViewState.Value>> values,
                                 ViewSubmissionPayload.User slackUser) {
        DatabaseConnectionInfo databaseConnectionInfo = selectClusterSchemaTableBlocks.getDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.getSchemaName(values);
        String oldTableName = selectClusterSchemaTableBlocks.getTableName(values);

        String newTableName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.RenameTable.renameTableNewTableNameTextInputId);

        RenameTableRequestDTO renameTableRequestDTO = new RenameTableRequestDTO(schemaName, oldTableName, newTableName);
        ddlValidator.validateRenameTable(databaseConnectionInfo, renameTableRequestDTO);
        ddlController.renameTable(databaseConnectionInfo, renameTableRequestDTO, slackUser);
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.RENAME_TABLE);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.RenameTable.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return;
    }
}
