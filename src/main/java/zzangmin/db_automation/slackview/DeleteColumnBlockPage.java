package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.DeleteColumnRequestDTO;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteColumnBlockPage {
    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String columnNameLabel = "input column name";
    private static final String columnNamePlaceholder = "id";

    public List<LayoutBlock> deleteColumnBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTable.selectClusterSchemaTableBlocks());

        // 컬럼명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.deleteColumnColumnNameTextInputId,
                columnNameLabel,
                columnNamePlaceholder));

        return blocks;
    }

    public void handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {

        String columnName = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.deleteColumnColumnNameTextInputId);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTable.getDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTable.getSchemaName(values);
        String tableName = selectClusterSchemaTable.getTableName(values);

        DeleteColumnRequestDTO deleteColumnRequestDTO = new DeleteColumnRequestDTO(schemaName, tableName, columnName);
        deleteColumnRequestDTO.setCommandType(CommandType_old.DELETE_COLUMN);
        ddlValidator.validateDeleteColumn(selectedDatabaseConnectionInfo, deleteColumnRequestDTO);
        ddlController.deleteColumn(selectedDatabaseConnectionInfo, deleteColumnRequestDTO);
    }

}
