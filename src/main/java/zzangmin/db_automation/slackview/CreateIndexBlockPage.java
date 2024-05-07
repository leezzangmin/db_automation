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
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.service.SlackService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateIndexBlockPage {

    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final DDLController ddlController;

    private static String createIndexIndexNameTextInputLabelId = "Index Name: ";
    private static String createIndexNamePlaceHolder = "idx_orderno_createdat";
    private static String createIndexTypePlaceHolder = "select index type";
    private static String createIndexColumnPlaceHolder = "input column name";

    public List<LayoutBlock> createIndexBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTable.selectClusterSchemaTableBlocks());
        List<OptionObject> indexTypeOptions = Arrays.stream(Constraint.ConstraintType.values())
                .map(constraintType -> OptionObject.builder()
                        .text(plainText(constraintType.getTypeName()))
                        .value(constraintType.getTypeName())
                        .build())
                .collect(Collectors.toList());
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findIndexTypeActionId,
                indexTypeOptions,
                createIndexTypePlaceHolder));
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexIndexNameTextInputId,
                createIndexIndexNameTextInputLabelId,
                createIndexNamePlaceHolder));
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexColumnNameTextInputId,
                SlackController.createIndexColumnNameTextInputId,
                createIndexColumnPlaceHolder));

        return blocks;
    }

    public List<LayoutBlock> handleAddColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return null;
    }

    public List<LayoutBlock> handleRemoveColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return null;
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

        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        ddlController.createIndex(selectedDatabaseConnectionInfo, createIndexRequestDTO);
        return null;
    }




}
