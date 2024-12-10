package zzangmin.db_automation.view.ddlpage;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.convention.CommonConvention;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.ddl.AddColumnRequestDTO;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.response.ddl.AddColumnDDLResponseDTO;
import zzangmin.db_automation.entity.mysqlobject.Column;
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

import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@RequiredArgsConstructor
@Component
public class AddColumnBlockPage implements BlockPage {

    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String columnNameLabel = "input column name";
    private static final String columnNamePlaceholder = "column_name";
    private static final String columnTypeLabel = "input column type";
    private static final String columnTypePlaceholder = "varchar(255)";
    private static final String columnCommentLabel = "input column comment";
    private static final String columnCommentPlaceholder = "column comment";
    private static final String columnIsNullLabel = "choose column nullable";
    private static final String columnDefaultValueLabel = "input column default value";
    private static final String columnDefaultValuePlaceholder = "12345";

    /**
     *  auto_increment, unique 컬럼 추가는 미지원
     *  -> [컬럼 추가 후 제약조건 추가] 2단계로 진행
     */
    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterSchemaTableBlocks());

        // 컬럼명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.AddColumn.addColumnColumnNameTextInputId,
                columnNameLabel,
                columnNamePlaceholder));

        // 컬럼 타입
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.AddColumn.addColumnColumnTypeTextInputId,
                columnTypeLabel,
                columnTypePlaceholder));

        // nullable
        List<OptionObject> options = new ArrayList<>();
        options.add(OptionObject.builder()
                        .text(plainText("true"))
                        .value("NULL")
                        .build());
        options.add(OptionObject.builder()
                .text(plainText("false"))
                .value("NOT NULL")
                .build());
        blocks.add(BasicBlockFactory.getRadioBlock(options,
                SlackConstants.CommandBlockIds.AddColumn.addColumnColumnIsNullRadioId,
                columnIsNullLabel));

        // default value
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.AddColumn.addColumnColumnDefaultValueTextInputId,
                columnDefaultValueLabel,
                columnDefaultValuePlaceholder));

        // 코멘트
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.AddColumn.addColumnColumnCommentTextInputId,
                columnCommentLabel,
                columnCommentPlaceholder));

// alter table table123 add column asdf123 varchar(100) null default 'asdf' unique comment 'asdf';

        return blocks;
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {

        String columnName = SlackMessageService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.AddColumn.addColumnColumnNameTextInputId);

        String columnType = SlackMessageService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.AddColumn.addColumnColumnTypeTextInputId);

        String nullable = SlackMessageService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.AddColumn.addColumnColumnIsNullRadioId);

        String defaultValue = SlackMessageService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.AddColumn.addColumnColumnDefaultValueTextInputId);

        String columnComment = SlackMessageService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.AddColumn.addColumnColumnCommentTextInputId);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.findSchemaName(values);
        String tableName = selectClusterSchemaTableBlocks.findTableName(values);

        Column column = Column.builder()
                .name(columnName)
                .type(columnType)
                .isNull(nullable.equals("NOT NULL") ? false : true)
                .defaultValue(defaultValue)
                .isAutoIncrement(false)
                .comment(columnComment)
                .collate(CommonConvention.COLLATE)
                .build();
        AddColumnRequestDTO addColumnRequestDTO = new AddColumnRequestDTO(schemaName, tableName, column);

        ddlValidator.validateAddColumn(selectedDatabaseConnectionInfo, addColumnRequestDTO);

        return addColumnRequestDTO;
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.ADD_COLUMN);
    }

    @Override
    public void handleViewAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return;
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.AddColumn.class)
                .contains(actionId);
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        List<LayoutBlock> blocks = new ArrayList<>();
        AddColumnRequestDTO addColumnRequestDTO = (AddColumnRequestDTO) requestDTO;

        String sql = addColumnRequestDTO.toSQL();
        blocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Content:* ```" + sql + "```",
                "AddColumnRequestDTO"));

        return blocks;
    }

    @Override
    public String execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        AddColumnDDLResponseDTO addColumnDDLResponseDTO = ddlController.addColumn(databaseConnectionInfo, (AddColumnRequestDTO) requestDTO, slackUserId);
        return addColumnDDLResponseDTO.getCreateStatement();
    }
}
