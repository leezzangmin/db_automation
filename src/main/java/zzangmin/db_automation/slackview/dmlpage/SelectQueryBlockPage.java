package zzangmin.db_automation.slackview.dmlpage;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DMLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.request.dml.SelectQueryRequestDTO;
import zzangmin.db_automation.dto.response.dml.SelectQueryResponseDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.BlockPage;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.slackview.globalpage.SelectClusterSchemaTableBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class SelectQueryBlockPage implements BlockPage {

    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final DMLController dmlController;

    private static final String selectSQLTextInputLabel = "SELECT QUERY";
    private static final String selectSQLPlaceHolder = "SELECT * FROM ...";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        // 클러스터, 스키마
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterSchemaBlocks());

        // SQL
        blocks.add(BasicBlockFactory.findMultilinePlainTextInput(SlackConstants.CommandBlockIds.SelectQuery.selectSQLTextInputId,
                selectSQLTextInputLabel,
                selectSQLPlaceHolder));

        return blocks;
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {
        String selectSQL = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.SelectQuery.selectSQLTextInputId);
        log.info("selectSQL: {}", selectSQL);
        SelectQueryRequestDTO selectQueryRequestDTO;
        try {
            selectQueryRequestDTO = SelectQueryRequestDTO.of(createTableStatementSQL);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        log.info("selectQueryRequestDTO: {}", selectQueryRequestDTO);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.findSchemaName(values);

        schemaName.setSchemaName(schemaName);
        ddlValidator.validateCreateTable(selectedDatabaseConnectionInfo, createTableRequestDTO);

        return createTableRequestDTO;
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return DatabaseRequestCommandGroup.CommandType.SELECT.equals(commandType);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.SelectQuery.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return;
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        List<LayoutBlock> blocks = new ArrayList<>();
        SelectQueryRequestDTO selectQueryRequestDTO = (SelectQueryRequestDTO) requestDTO;

        blocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Content:* `" + selectQueryRequestDTO.toSQL() + "`", "ShowGrantBlockPage"));

        return blocks;
    }

    @Override
    public String execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        SelectQueryResponseDTO selectQueryResponseDTO = dmlController.select(databaseConnectionInfo, (SelectQueryRequestDTO) requestDTO, slackUserId);
        return selectQueryResponseDTO.toString();
    }
}
