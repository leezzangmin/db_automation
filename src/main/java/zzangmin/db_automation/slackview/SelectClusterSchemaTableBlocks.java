package zzangmin.db_automation.slackview;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SlackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@RequiredArgsConstructor
@Component
public class SelectClusterSchemaTableBlocks {
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
        StaticSelectElement clusterSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findClusterSelectsElementActionId,
                clusterOptions,
                clusterPlaceholder);

        List<OptionObject> emptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        StaticSelectElement schemaSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId,
                emptyOption,
                schemaPlaceholder);

        StaticSelectElement tableSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findTableSelectsElementActionId,
                emptyOption,
                tablePlaceholder);

        String tableSchemaLabelText = "<Table Schema>";
        ContextBlock contextBlock = BasicBlockFactory.getContextBlock(tableSchemaLabelText, SlackConstants.CommandBlockIds.tableSchemaContextId);
        SectionBlock textSection = BasicBlockFactory.getTextSection("choose table first", SlackConstants.CommandBlockIds.tableSchemaTextId);

        blocks.add(actions(List.of(clusterSelectElement, schemaSelectElement, tableSelectElement)));
        blocks.add(contextBlock);
        blocks.add(textSection);
        return blocks;
    }

    public List<LayoutBlock> selectClusterSchemaBlocks() {
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
        StaticSelectElement clusterSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findClusterSelectsElementActionId,
                clusterOptions,
                clusterPlaceholder);

        List<OptionObject> emptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        StaticSelectElement schemaSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId,
                emptyOption,
                schemaPlaceholder);

        blocks.add(actions(List.of(clusterSelectElement, schemaSelectElement)));
        return blocks;
    }

    public List<LayoutBlock> handleClusterChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        setSchemaNameOptions(currentBlocks, values);
        try {
            SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.tableSchemaTextId);
        } catch (Exception e) {
            log.info("table schema element 없음");
            return currentBlocks;
        }
        resetTableSchemaSectionBlock(currentBlocks);
        return currentBlocks;
    }

    public List<LayoutBlock> handleSchemaChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        try {
            SlackService.findBlockIndex(currentBlocks, "actions", SlackConstants.CommandBlockIds.findTableSelectsElementActionId);
        } catch (Exception e) {
            log.info("table element 없음");
            return currentBlocks;
        }
        setTableNameOptions(currentBlocks, values);
        try {
            SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.tableSchemaTextId);
        } catch (Exception e) {
            log.info("table schema element 없음");
            return currentBlocks;
        }
        resetTableSchemaSectionBlock(currentBlocks);
        return currentBlocks;
    }

    public DatabaseConnectionInfo getDatabaseConnectionInfo(Map<String, Map<String, ViewState.Value>> values) {
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        return selectedDatabaseConnectionInfo;
    }

    public String getSchemaName(Map<String, Map<String, ViewState.Value>> values) {
        String schemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId);
        return schemaName;
    }

    public String getTableName(Map<String, Map<String, ViewState.Value>> values) {
        String tableName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findTableSelectsElementActionId);
        return tableName;
    }

    public List<LayoutBlock> handleTableChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        Integer selectTableNameBlockIndex = null;
        try {
            selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.tableSchemaTextId);
        } catch (Exception e) {
            log.info("테이블 최초 선택");
            return currentBlocks;
        }
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId);
        String selectedTableName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findTableSelectsElementActionId);
        SectionBlock tableSchema = (SectionBlock) fetchTableSchemaBlocks(selectedDatabaseConnectionInfo, selectedSchemaName, selectedTableName).get(1);
        currentBlocks.set(selectTableNameBlockIndex, tableSchema);
        return currentBlocks;
    }

    private List<LayoutBlock> fetchTableSchemaBlocks(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        List<LayoutBlock> blocks = new ArrayList<>();
        String tableSchemaLabelText = "<Table Schema>";
        ContextBlock contextBlock = BasicBlockFactory.getContextBlock(tableSchemaLabelText, SlackConstants.CommandBlockIds.tableSchemaContextId);
        blocks.add(contextBlock);

        String tableSchema = describeService.findTableSchema(databaseConnectionInfo, schemaName, tableName);
        SectionBlock textSection = BasicBlockFactory.getTextSection(tableSchema, SlackConstants.CommandBlockIds.tableSchemaTextId);
        blocks.add(textSection);

        return blocks;
    }

    private void setSchemaNameOptions(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String DBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findClusterSelectsElementActionId);
        log.info("DBMSName: {}", DBMSName);
        DatabaseConnectionInfo databaseConnectionInfo = DynamicDataSourceProperties.findByDbName(DBMSName);
        List<OptionObject> schemaNameOptions = fetchSchemaNameOptions(databaseConnectionInfo);
        StaticSelectElement schemaSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId,
                schemaNameOptions,
                schemaPlaceholder);
        log.info("schemaSelectElement: {}", schemaSelectElement);
        int schemaSelectBlockIndex = SlackService.findBlockIndex(currentBlocks,
                "actions",
                SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId);
        ActionsBlock actionsBlock = (ActionsBlock) currentBlocks.get(schemaSelectBlockIndex);
        List<BlockElement> elements = actionsBlock.getElements();

        int schemaElementIndex = SlackService.findElementIndex(elements, SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId);
        elements.set(schemaElementIndex, schemaSelectElement);
        actionsBlock.setElements(elements);
        currentBlocks.set(schemaSelectBlockIndex, actionsBlock);
    }

    private static void resetTableSchemaSectionBlock(List<LayoutBlock> currentBlocks) {
        int tableSchemaTextSectionIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.tableSchemaTextId);
        SectionBlock textSection = BasicBlockFactory.getTextSection("choose table first", SlackConstants.CommandBlockIds.tableSchemaTextId);
        currentBlocks.set(tableSchemaTextSectionIndex, textSection);
    }

    private void setTableNameOptions(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId);
        List<OptionObject> tableNameOptions = fetchTableNameOptions(selectedDatabaseConnectionInfo, selectedSchemaName);

        int selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks,
                "actions",
                SlackConstants.CommandBlockIds.findTableSelectsElementActionId);

        StaticSelectElement tableSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.findTableSelectsElementActionId,
                tableNameOptions,
                tablePlaceholder);
//
        ActionsBlock actionsBlock = (ActionsBlock) currentBlocks.get(selectTableNameBlockIndex);
        List<BlockElement> elements = actionsBlock.getElements();

        int tableElementIndex = SlackService.findElementIndex(elements, SlackConstants.CommandBlockIds.findTableSelectsElementActionId);
        elements.set(tableElementIndex, tableSelectElement);
        actionsBlock.setElements(elements);
        currentBlocks.set(selectTableNameBlockIndex, actionsBlock);
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
