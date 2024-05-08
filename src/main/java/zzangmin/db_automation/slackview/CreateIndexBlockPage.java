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
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexColumnNameTextInputId + 1,
                inputIndexColumnNameLabel + 1,
                createIndexColumnPlaceHolder));
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
                                )))));

        return blocks;
    }

    public List<LayoutBlock> handleAddColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        int lastInputColumnNameBlockIndex = findLastInputColumnNameBlockIndex(currentBlocks);
        int blockIdNumber = findBlockIdNumber(currentBlocks.get(lastInputColumnNameBlockIndex));
        currentBlocks.add(lastInputColumnNameBlockIndex, BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexColumnNameTextInputId + blockIdNumber,
                inputIndexColumnNameLabel + blockIdNumber,
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
            return index;
        }
        if (index == -1) {
            throw new IllegalArgumentException("column name inputBlock 이 존재하지 않습니다.");
        }
        return index;
    }

    private int findBlockIdNumber(LayoutBlock block) {
        String blockId = block.getBlockId();
        blockId.replace(SlackController.createIndexColumnNameTextInputId, "");
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


        CreateIndexRequestDTO createIndexRequestDTO = CreateIndexRequestDTO.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .indexName(indexName)
                .indexType(indexType)
                .columnNames(List.of("name"))
                .build();
        createIndexRequestDTO.setCommandType(CommandType_old.CREATE_INDEX);
        log.info("createIndexRequestDTO: {}", createIndexRequestDTO);
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        log.info("selectedDBMSName: {}", selectedDBMSName);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        log.info("selectedDatabaseConnectionInfo: {}", selectedDatabaseConnectionInfo);
        ddlController.createIndex(selectedDatabaseConnectionInfo, createIndexRequestDTO);
        return null;
    }

    private List<LayoutBlock> startMessageBlocks() {
        return null;
    }

    private List<LayoutBlock> endMessageBlocks() {
        return null;
    }



}
