package zzangmin.db_automation.slackview.commandpage;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.DeleteColumnRequestDTO;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SelectClusterSchemaTableBlocks;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteColumnBlockPage implements BlockPage {
    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String columnNameLabel = "input column name";
    private static final String columnNamePlaceholder = "id";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterSchemaTableBlocks());

        // 컬럼명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.deleteColumnColumnNameTextInputId,
                columnNameLabel,
                columnNamePlaceholder));

        return blocks;
    }

    @Override
    public void handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {

        String columnName = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.deleteColumnColumnNameTextInputId);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.getDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.getSchemaName(values);
        String tableName = selectClusterSchemaTableBlocks.getTableName(values);

        DeleteColumnRequestDTO deleteColumnRequestDTO = new DeleteColumnRequestDTO(schemaName, tableName, columnName);
        deleteColumnRequestDTO.setCommandType(CommandType_old.DELETE_COLUMN);
        ddlValidator.validateDeleteColumn(selectedDatabaseConnectionInfo, deleteColumnRequestDTO);
        ddlController.deleteColumn(selectedDatabaseConnectionInfo, deleteColumnRequestDTO);
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.DELETE_COLUMN);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return;
    }
}
