package zzangmin.db_automation.slackview.commandpage;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.CreateIndexRequestDTO;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SelectClusterSchemaTableBlocks;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateIndexBlockPage implements BlockPage {

    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String createIndexIndexNameTextInputLabel = "Index Name";
    private static final String createIndexNamePlaceHolder = "idx_orderno_createdat";
    private static final String createIndexTypePlaceHolder = "select index type";
    private static final String createIndexColumnPlaceHolder = "input column name";
    private static final String inputIndexColumnNameLabel = "Column Name ";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterSchemaTableBlocks());

        List<OptionObject> indexTypeOptions = Arrays.stream(Constraint.ConstraintType.values())
                .map(constraintType -> OptionObject.builder()
                        .text(plainText(constraintType.name()))
                        .value(constraintType.name())
                        .build())
                .collect(Collectors.toList());

        // 인덱스 타입
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.CreateIndex.findIndexTypeActionId,
                indexTypeOptions,
                createIndexTypePlaceHolder));

        // 인덱스 명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.CreateIndex.createIndexIndexNameTextInputId,
                createIndexIndexNameTextInputLabel,
                createIndexNamePlaceHolder));

        // 인덱스 컬럼명
        blocks.add(getInitialIndexColumnNameInputBlock());

        // 컬럼 추가, 삭제 버튼
        blocks.add(
                actions(actions -> actions
                        .elements(asElements(
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("컬럼 추가")))
                                        .value(SlackConstants.CommandBlockIds.CreateIndex.createIndexAddColumnButtonId)
                                        .style("primary")
                                        .actionId(SlackConstants.CommandBlockIds.CreateIndex.createIndexAddColumnButtonId)
                                ),
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("컬럼 삭제")))
                                        .value(SlackConstants.CommandBlockIds.CreateIndex.createIndexRemoveColumnButtonId)
                                        .style("danger")
                                        .actionId(SlackConstants.CommandBlockIds.CreateIndex.createIndexRemoveColumnButtonId)
                                )))
                        .blockId(SlackConstants.CommandBlockIds.CreateIndex.createIndexRemoveColumnButtonId)));
        return blocks;

    }

    @Override
    public void handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String indexName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.CreateIndex.createIndexIndexNameTextInputId);
        log.info("indexName: {}", indexName);

        String indexType = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.CreateIndex.findIndexTypeActionId);
        log.info("indexType: {}", indexType);

        List<String> indexColumnNames = findIndexColumnNames(currentBlocks, values);
        log.info("indexColumnNames: {}", indexColumnNames);

        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.getDatabaseConnectionInfo(values);
        String schemaName = selectClusterSchemaTableBlocks.getSchemaName(values);
        String tableName = selectClusterSchemaTableBlocks.getTableName(values);

        CreateIndexRequestDTO createIndexRequestDTO = CreateIndexRequestDTO.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .indexName(indexName)
                .indexType(indexType)
                .columnNames(indexColumnNames)
                .build();
        createIndexRequestDTO.setCommandType(CommandType_old.CREATE_INDEX);
        log.info("createIndexRequestDTO: {}", createIndexRequestDTO);

        ddlValidator.validateDDLRequest(selectedDatabaseConnectionInfo, createIndexRequestDTO);
        ddlController.createIndex(selectedDatabaseConnectionInfo, createIndexRequestDTO);
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_INDEX);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.CreateIndex.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        if (actionId.equals(SlackConstants.CommandBlockIds.CreateIndex.createIndexAddColumnButtonId)) {
            handleAddColumn(currentBlocks);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.CreateIndex.createIndexRemoveColumnButtonId)) {
            handleRemoveColumn(currentBlocks, values);
        } else {
            throw new IllegalArgumentException("미지원 actionId: " + actionId);
        }
    }

    public List<LayoutBlock> handleAddColumn(List<LayoutBlock> currentBlocks) {
        int lastInputColumnNameBlockIndex;
        try {
            lastInputColumnNameBlockIndex = findLastInputColumnNameBlockIndex(currentBlocks);
        } catch (Exception e) {
            int inputColumnNameIndex = SlackService.findBlockIndex(currentBlocks, "input", SlackConstants.CommandBlockIds.CreateIndex.createIndexIndexNameTextInputId) + 1;
            currentBlocks.add(inputColumnNameIndex, getInitialIndexColumnNameInputBlock());
            return currentBlocks;
        }
        log.info("lastInputColumnNameBlockIndex: {}", lastInputColumnNameBlockIndex);
        int blockIdNumber = findBlockIdNumber(currentBlocks.get(lastInputColumnNameBlockIndex));
        currentBlocks.add(lastInputColumnNameBlockIndex + 1, BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.CreateIndex.createIndexColumnNameTextInputId + (blockIdNumber + 1),
                inputIndexColumnNameLabel + (blockIdNumber + 1),
                createIndexColumnPlaceHolder));
        return currentBlocks;
    }

    private int findLastInputColumnNameBlockIndex(List<LayoutBlock> currentBlocks) {
        int index = -1;
        try {
            for (int i = 1; i < 99999999; i++) {
                index = SlackService.findBlockIndex(currentBlocks,
                        "input",
                        SlackConstants.CommandBlockIds.CreateIndex.createIndexColumnNameTextInputId + i);
            }
        } catch (Exception e) {
        }
        if (index == -1) {
            throw new IllegalArgumentException("column name inputBlock 이 존재하지 않습니다.");
        }
        return index;
    }

    private LayoutBlock getInitialIndexColumnNameInputBlock() {
/**
 * actions block error:
 * Invalid value: "plain_text_input".
 * Must be one of: "static_select", "users_select",
 * "conversations_select", "channels_select", "external_select", "button",
 * "workflow_button", "overflow", "datepicker", "radio_buttons",
 * "checkboxes", "range_datepicker", "timepicker", "multi_static_select",
 * "multi_users_select", "multi_conversations_select",
 * "multi_channels_select", "multi_external_select", "datetimepicker"
 */
//        return actions(actions -> actions
//                .elements(asElements(
//                        button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
//                                .value("deliveryTip.getSeq().toString()")
//                                .style("primary")
//                                .text(plainText("ddd"))
//                                .actionId("aaa")
//                        ),
//                        BasicBlockFactory.findSinglelinePlainTextInput2("asdf", "dfdf", "pp")
//                ))
//        );

        return BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.CreateIndex.createIndexColumnNameTextInputId + 1,
                inputIndexColumnNameLabel + 1,
                createIndexColumnPlaceHolder);
    }

    private int findBlockIdNumber(LayoutBlock block) {
        String blockId = block.getBlockId();
        blockId = blockId.replace(SlackConstants.CommandBlockIds.CreateIndex.createIndexColumnNameTextInputId, "");
        return Integer.parseInt(blockId);
    }

    public List<LayoutBlock> handleRemoveColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        int lastInputColumnNameBlockIndex = findLastInputColumnNameBlockIndex(currentBlocks);
        currentBlocks.remove(lastInputColumnNameBlockIndex);
        return currentBlocks;
    }

    private List<String> findIndexColumnNames(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        List<String> indexColumnNames = new ArrayList<>();
        for (int i = 1;i < findLastInputColumnNameBlockIndex(currentBlocks);i++) {
            try {
                String columnName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.CreateIndex.createIndexColumnNameTextInputId + i);
                indexColumnNames.add(columnName);
            } catch (Exception e) {
                break;
            }
        }
        return indexColumnNames;
    }

    private List<LayoutBlock> startMessageBlocks() {
        return null;
    }

    private List<LayoutBlock> endMessageBlocks() {
        return null;
    }


}
