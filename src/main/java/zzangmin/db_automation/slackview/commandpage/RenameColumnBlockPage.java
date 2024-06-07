package zzangmin.db_automation.slackview.commandpage;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.ddl.RenameColumnRequestDTO;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.response.ddl.RenameColumnDDLResponseDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.slackview.globalpage.SelectClusterSchemaTableBlocks;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RenameColumnBlockPage implements BlockPage {

    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String oldColumnNameLabel = "input old column name";
    private static final String oldColumnNamePlaceholder = "old_column_name";

    private static final String newColumnNameLabel = "input new column name";
    private static final String newColumnNamePlaceholder = "new_column_name";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterSchemaTableBlocks());

        // 변경 대상 컬럼명 (old)
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.RenameColumn.renameColumnOldColumnNameTextInputId,
                oldColumnNameLabel,
                oldColumnNamePlaceholder));

        // 바꿀 컬럼명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.RenameColumn.renameColumnNewColumnNameTextInputId,
                newColumnNameLabel,
                newColumnNamePlaceholder));

        return blocks;
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {

        String oldColumnName = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.RenameColumn.renameColumnOldColumnNameTextInputId);

        String newColumnName = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.RenameColumn.renameColumnNewColumnNameTextInputId);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.findSchemaName(values);
        String tableName = selectClusterSchemaTableBlocks.findTableName(values);


        RenameColumnRequestDTO renameColumnRequestDTO = new RenameColumnRequestDTO(schemaName, tableName, oldColumnName, newColumnName);

        ddlValidator.validateRenameColumn(selectedDatabaseConnectionInfo, renameColumnRequestDTO);
        return renameColumnRequestDTO;
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.RENAME_COLUMN);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.RenameColumn.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return;
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        List<LayoutBlock> blocks = new ArrayList<>();
        RenameColumnRequestDTO renameColumnRequestDTO = (RenameColumnRequestDTO) requestDTO;
        String sql = renameColumnRequestDTO.toSQL();
        blocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Content:* ```" + sql + "```", "renameColumnRequestDTO"));
        return blocks;
    }

    @Override
    public String execute(DatabaseConnectionInfo selectedDatabaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        RenameColumnRequestDTO renameColumnRequestDTO = (RenameColumnRequestDTO) requestDTO;
        RenameColumnDDLResponseDTO renameColumnDDLResponseDTO = ddlController.renameColumn(selectedDatabaseConnectionInfo, renameColumnRequestDTO, slackUserId);
        return renameColumnDDLResponseDTO.getCreateStatement();
    }

}
