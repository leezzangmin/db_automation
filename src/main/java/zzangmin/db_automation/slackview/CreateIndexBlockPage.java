package zzangmin.db_automation.slackview;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.controller.SlackController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.CreateIndexRequestDTO;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.service.SlackService;
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
public class CreateIndexBlockPage {

    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;
    private static String createIndexIndexNameTextInputLabel = "Index Name";
    private static String createIndexNamePlaceHolder = "idx_orderno_createdat";
    private static String createIndexTypePlaceHolder = "select index type";
    private static String createIndexColumnPlaceHolder = "input column name";
    private static String inputIndexColumnNameLabel = "Column Name ";

    public List<LayoutBlock> createIndexBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTable.selectClusterSchemaTableBlocks());
        List<OptionObject> indexTypeOptions = Arrays.stream(Constraint.ConstraintType.values())
                .map(constraintType -> OptionObject.builder()
                        .text(plainText(constraintType.name()))
                        .value(constraintType.name())
                        .build())
                .collect(Collectors.toList());
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findIndexTypeActionId,
                indexTypeOptions,
                createIndexTypePlaceHolder));
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexIndexNameTextInputId,
                createIndexIndexNameTextInputLabel,
                createIndexNamePlaceHolder));
        blocks.add(getInitialIndexColumnNameInputBlock());
        blocks.add(
                actions(actions -> actions
                        .elements(asElements(
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("컬럼 추가")))
                                        .value(SlackController.createIndexAddColumnButtonId)
                                        .style("primary")
                                        .actionId(SlackController.createIndexAddColumnButtonId)
                                ),
                                button(b -> b.text(plainText(pt -> pt.emoji(true).text("컬럼 삭제")))
                                        .value(SlackController.createIndexRemoveColumnButtonId)
                                        .style("danger")
                                        .actionId(SlackController.createIndexRemoveColumnButtonId)
                                )))
                        .blockId(SlackController.createIndexRemoveColumnButtonId)));

        return blocks;
    }

    public List<LayoutBlock> handleAddColumn(List<LayoutBlock> currentBlocks) {
        int lastInputColumnNameBlockIndex;
        try {
            lastInputColumnNameBlockIndex = findLastInputColumnNameBlockIndex(currentBlocks);
        } catch (Exception e) {
            int inputColumnNameIndex = SlackService.findBlockIndex(currentBlocks, "input", SlackController.createIndexIndexNameTextInputId) + 1;
            currentBlocks.add(inputColumnNameIndex, getInitialIndexColumnNameInputBlock());
            return currentBlocks;
        }
        log.info("lastInputColumnNameBlockIndex: {}", lastInputColumnNameBlockIndex);
        int blockIdNumber = findBlockIdNumber(currentBlocks.get(lastInputColumnNameBlockIndex));
        currentBlocks.add(lastInputColumnNameBlockIndex + 1, BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexColumnNameTextInputId + (blockIdNumber + 1),
                inputIndexColumnNameLabel + (blockIdNumber + 1),
                createIndexColumnPlaceHolder));
        return currentBlocks;
    }

    private int findLastInputColumnNameBlockIndex(List<LayoutBlock> currentBlocks) {
        int index = -1;
        try {
            for (int i = 1; i < 99999999; i++) {
                index = SlackService.findBlockIndex(currentBlocks, "input", SlackController.createIndexColumnNameTextInputId + i);
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

        return BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexColumnNameTextInputId + 1,
                inputIndexColumnNameLabel + 1,
                createIndexColumnPlaceHolder);
    }

    private int findBlockIdNumber(LayoutBlock block) {
        String blockId = block.getBlockId();
        blockId = blockId.replace(SlackController.createIndexColumnNameTextInputId, "");
        return Integer.parseInt(blockId);
    }

    public List<LayoutBlock> handleRemoveColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        int lastInputColumnNameBlockIndex = findLastInputColumnNameBlockIndex(currentBlocks);
        currentBlocks.remove(lastInputColumnNameBlockIndex);
        return currentBlocks;
    }

    public List<LayoutBlock> handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String indexName = SlackService.findCurrentValueFromState(values, SlackController.createIndexIndexNameTextInputId);
        log.info("indexName: {}", indexName);

        String indexType = SlackService.findCurrentValueFromState(values, SlackController.findIndexTypeActionId);
        log.info("indexType: {}", indexType);

        String schemaName = SlackService.findCurrentValueFromState(values, SlackController.findSchemaSelectsElementActionId);
        log.info("schemaName: {}", schemaName);

        String tableName = SlackService.findCurrentValueFromState(values, SlackController.findTableSelectsElementActionId);
        log.info("tableName: {}", tableName);

        List<String> indexColumnNames = findIndexColumnNames(currentBlocks, values);
        log.info("indexColumnNames: {}", indexColumnNames);
        CreateIndexRequestDTO createIndexRequestDTO = CreateIndexRequestDTO.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .indexName(indexName)
                .indexType(indexType)
                .columnNames(indexColumnNames)
                .build();
        createIndexRequestDTO.setCommandType(CommandType_old.CREATE_INDEX);
        log.info("createIndexRequestDTO: {}", createIndexRequestDTO);
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        log.info("selectedDBMSName: {}", selectedDBMSName);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        log.info("selectedDatabaseConnectionInfo: {}", selectedDatabaseConnectionInfo);

        ddlValidator.validateDDLRequest(selectedDatabaseConnectionInfo, createIndexRequestDTO);
        ddlController.createIndex(selectedDatabaseConnectionInfo, createIndexRequestDTO);
        return null;
    }

    private List<String> findIndexColumnNames(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        List<String> indexColumnNames = new ArrayList<>();
        for (int i = 1;i < findLastInputColumnNameBlockIndex(currentBlocks);i++) {
            try {
                String columnName = SlackService.findCurrentValueFromState(values, SlackController.createIndexColumnNameTextInputId + i);
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
