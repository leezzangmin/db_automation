package zzangmin.db_automation.view.ddlpage;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.ddl.DeleteColumnRequestDTO;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.response.ddl.DeleteColumnDDLResponseDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackMessageService;
import zzangmin.db_automation.view.BasicBlockFactory;
import zzangmin.db_automation.view.BlockPage;
import zzangmin.db_automation.view.SlackConstants;
import zzangmin.db_automation.view.globalpage.SelectClusterSchemaTableBlocks;
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
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.DeleteColumn.deleteColumnColumnNameTextInputId,
                columnNameLabel,
                columnNamePlaceholder));

        return blocks;
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {

        String columnName = SlackMessageService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.DeleteColumn.deleteColumnColumnNameTextInputId);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.findSchemaName(values);
        String tableName = selectClusterSchemaTableBlocks.findTableName(values);

        DeleteColumnRequestDTO deleteColumnRequestDTO = new DeleteColumnRequestDTO(schemaName, tableName, columnName);
        ddlValidator.validateDeleteColumn(selectedDatabaseConnectionInfo, deleteColumnRequestDTO);
        return deleteColumnRequestDTO;
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.DELETE_COLUMN);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.DeleteColumn.class)
                .contains(actionId);
    }

    @Override
    public void handleViewAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return;
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        List<LayoutBlock> blocks = new ArrayList<>();
        DeleteColumnRequestDTO deleteColumnRequestDTO = (DeleteColumnRequestDTO) requestDTO;
        String sql = deleteColumnRequestDTO.toSQL();
        blocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Content:* ```" + sql + "```",
                "DeleteColumnRequestDTO"));
        return blocks;
    }

    @Override
    public String execute(DatabaseConnectionInfo selectedDatabaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        DeleteColumnRequestDTO deleteColumnRequestDTO = (DeleteColumnRequestDTO) requestDTO;
        DeleteColumnDDLResponseDTO deleteColumnDDLResponseDTO = ddlController.deleteColumn(selectedDatabaseConnectionInfo, deleteColumnRequestDTO, slackUserId);
        return deleteColumnDDLResponseDTO.getCreateStatement();
    }
}
