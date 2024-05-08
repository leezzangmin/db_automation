package zzangmin.db_automation.slackview;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.controller.SlackController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SlackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static zzangmin.db_automation.controller.SlackController.findSchemaSelectsElementActionId;

@Slf4j
@RequiredArgsConstructor
@Component
public class SelectClusterSchemaTable {
    private final DescribeService describeService;

    private final String clusterPlaceholder = "select cluster";
    private final String schemaPlaceholder = "select schema";
    private final String tablePlaceholder = "select table";

    public List<LayoutBlock> selectClusterSchemaTableBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        List<OptionObject> clusterOptions = describeService.findDBMSNames()
                .getDbmsNames()
                .stream()
                .map(dbmsName -> OptionObject.builder()
                        .text(plainText(dbmsName))
                        .value(dbmsName)
                        .build()
                )
                .collect(Collectors.toList());
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findClusterSelectsElementActionId, clusterOptions, clusterPlaceholder));

        List<OptionObject> emptyOption = BasicBlockFactory.generateEmptyOptionObjects();

        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findSchemaSelectsElementActionId,
                emptyOption,
                schemaPlaceholder));
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findTableSelectsElementActionId,
                emptyOption,
                tablePlaceholder));

        String tableSchemaLabelText = "<Table Schema>";
        ContextBlock contextBlock = BasicBlockFactory.getContextBlock(tableSchemaLabelText, SlackController.tableSchemaContextId);
        blocks.add(contextBlock);

        SectionBlock textSection = BasicBlockFactory.getTextSection("choose table first", SlackController.tableSchemaTextId);
        blocks.add(textSection);
        return blocks;
    }

    public List<LayoutBlock> fetchTableSchemaBlocks(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        List<LayoutBlock> blocks = new ArrayList<>();
        String tableSchemaLabelText = "<Table Schema>";
        ContextBlock contextBlock = BasicBlockFactory.getContextBlock(tableSchemaLabelText, SlackController.tableSchemaContextId);
        blocks.add(contextBlock);

        String tableSchema = describeService.findTableSchema(databaseConnectionInfo, schemaName, tableName);
        SectionBlock textSection = BasicBlockFactory.getTextSection(tableSchema, SlackController.tableSchemaTextId);
        blocks.add(textSection);

        return blocks;
    }

    public List<LayoutBlock> handleClusterChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        setSchemaNameOptions(currentBlocks, values);
        resetTableSchemaSectionBlock(currentBlocks);
        return currentBlocks;
    }

    public List<LayoutBlock> handleSchemaChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        setTableNameOptions(currentBlocks, values);
        resetTableSchemaSectionBlock(currentBlocks);
        return currentBlocks;
    }

    public List<LayoutBlock> handleTableChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        Integer selectTableNameBlockIndex = null;
        try {
            selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackController.tableSchemaTextId);
        } catch (Exception e) {
            log.info("테이블 최초 선택");
            return currentBlocks;
        }
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackController.findSchemaSelectsElementActionId);
        String selectedTableName = SlackService.findCurrentValueFromState(values, SlackController.findTableSelectsElementActionId);
        SectionBlock tableSchema = (SectionBlock) fetchTableSchemaBlocks(selectedDatabaseConnectionInfo, selectedSchemaName, selectedTableName).get(1);
        currentBlocks.set(selectTableNameBlockIndex, tableSchema);
        return currentBlocks;
    }

    private void setSchemaNameOptions(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String DBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        log.info("DBMSName: {}", DBMSName);
        DatabaseConnectionInfo databaseConnectionInfo = DynamicDataSourceProperties.findByDbName(DBMSName);
        List<OptionObject> schemaNameOptions = fetchSchemaNameOptions(databaseConnectionInfo);
        ActionsBlock schemaSelects = BasicBlockFactory.findStaticSelectsBlock(findSchemaSelectsElementActionId,
                schemaNameOptions,
                schemaPlaceholder);
        log.info("schemaSelects: {}", schemaSelects);
        int schemaSelectIndex = SlackService.findBlockIndex(currentBlocks, "actions", SlackController.findSchemaSelectsElementActionId);
        currentBlocks.set(schemaSelectIndex, schemaSelects);
    }

    private static void resetTableSchemaSectionBlock(List<LayoutBlock> currentBlocks) {
        int tableSchemaTextSectionIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackController.tableSchemaTextId);
        SectionBlock textSection = BasicBlockFactory.getTextSection("choose table first", SlackController.tableSchemaTextId);
        currentBlocks.set(tableSchemaTextSectionIndex, textSection);
    }

    private void setTableNameOptions(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        int selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks, "actions", SlackController.findTableSelectsElementActionId);
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackController.findSchemaSelectsElementActionId);
        List<OptionObject> tableNameOptions = fetchTableNameOptions(selectedDatabaseConnectionInfo, selectedSchemaName);
        ActionsBlock tableSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackController.findTableSelectsElementActionId,
                tableNameOptions,
                tablePlaceholder);
        currentBlocks.set(selectTableNameBlockIndex, tableSelectBlock);
    }

    private List<OptionObject> fetchSchemaNameOptions(DatabaseConnectionInfo databaseConnectionInfo) {
        List<String> schemaNames = describeService.findSchemaNames(databaseConnectionInfo).getSchemaNames();
        if (schemaNames.isEmpty()) {
            return BasicBlockFactory.generateEmptyOptionObjects();
        }
        List<OptionObject> schemaNameOptions = schemaNames.stream()
                .map(schemaName -> OptionObject.builder()
                        .value(schemaName)
                        .text(plainText(schemaName))
                        .build())
                .collect(Collectors.toList());
        return schemaNameOptions;
    }

    private List<OptionObject> fetchTableNameOptions(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        List<String> tableNames = describeService.findTableNames(databaseConnectionInfo, schemaName)
                .getTableNames()
                .stream()
                .toList();
        if (tableNames.isEmpty()) {
            return BasicBlockFactory.generateEmptyOptionObjects();
        }
        return tableNames.stream()
                .map(tableName -> OptionObject.builder()
                        .value(tableName)
                        .text(plainText(tableName))
                        .build())
                .collect(Collectors.toList());
    }
}
