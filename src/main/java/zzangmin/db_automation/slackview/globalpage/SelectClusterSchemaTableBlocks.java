package zzangmin.db_automation.slackview.globalpage;

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
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
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.DescribeService;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.slackview.commandpage.BlockPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@RequiredArgsConstructor
@Component
public class SelectClusterSchemaTableBlocks implements BlockPage {
    private final DescribeService describeService;

    private final String clusterPlaceholder = "select cluster";
    private final String schemaPlaceholder = "select schema";
    private final String tablePlaceholder = "select table";

    @Override
    public List<LayoutBlock> generateBlocks() {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public void handleSubmission(List<LayoutBlock> currentBlocks,
                                 Map<String, Map<String, ViewState.Value>> values,
                                 ViewSubmissionPayload.User slackUser) {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return false;
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.ClusterSchemaTable.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        if (actionId.equals(SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId)) {
            handleClusterChange(currentBlocks, values);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId)) {
            handleSchemaChange(currentBlocks, values);
        } else if (actionId.equals(SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId)) {
            handleTableChange(currentBlocks, values);
        } else {
            throw new IllegalArgumentException("미지원 actionId: " + actionId);
        }
    }

    public List<LayoutBlock> selectClusterSchemaTableBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        List<OptionObject> clusterOptions = describeService.findDBMSNames()
                .getDbmsNames()
                .stream()
                .map(dbmsName -> OptionObject.builder()
                        .text(plainText(dbmsName))
                        .value(dbmsName)
                        .build())
                .collect(Collectors.toList());
        StaticSelectElement clusterSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId,
                clusterOptions,
                clusterPlaceholder);

        List<OptionObject> emptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        StaticSelectElement schemaSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId,
                emptyOption,
                schemaPlaceholder);

        StaticSelectElement tableSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId,
                emptyOption,
                tablePlaceholder);

        String tableSchemaLabelText = "<Table Schema>";
        ContextBlock contextBlock = BasicBlockFactory.getContextBlock(tableSchemaLabelText, SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaContextId);
        SectionBlock textSection = BasicBlockFactory.getTextSection("choose table first", SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);

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
        StaticSelectElement clusterSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId,
                clusterOptions,
                clusterPlaceholder);

        List<OptionObject> emptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        StaticSelectElement schemaSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId,
                emptyOption,
                schemaPlaceholder);

        blocks.add(actions(List.of(clusterSelectElement, schemaSelectElement)));
        return blocks;
    }

    public List<LayoutBlock> selectClusterBlocks() {
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
        StaticSelectElement clusterSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId,
                clusterOptions,
                clusterPlaceholder);

        blocks.add(actions(List.of(clusterSelectElement)));
        return blocks;
    }

    public DatabaseConnectionInfo getDatabaseConnectionInfo(Map<String, Map<String, ViewState.Value>> values) {
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        return selectedDatabaseConnectionInfo;
    }

    public String getSchemaName(Map<String, Map<String, ViewState.Value>> values) {
        String schemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId);
        return schemaName;
    }

    public String getTableName(Map<String, Map<String, ViewState.Value>> values) {
        String tableName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId);
        return tableName;
    }

    private List<LayoutBlock> handleClusterChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        boolean isSchemaBlockPresent = currentBlocks.stream()
                .filter(b -> b.getBlockId()
                        .equals(SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId))
                .findAny()
                .isPresent();
        if (isSchemaBlockPresent) {
            setSchemaNameOptions(currentBlocks, values);
        }

        try {
            SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);
        } catch (Exception e) {
            log.info("table schema element 없음");
            return currentBlocks;
        }
        resetTableSchemaSectionBlock(currentBlocks);
        return currentBlocks;
    }

    private List<LayoutBlock> handleSchemaChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        try {
            SlackService.findBlockIndex(currentBlocks, "actions", SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId);
        } catch (Exception e) {
            log.info("table element 없음");
            return currentBlocks;
        }
        setTableNameOptions(currentBlocks, values);
        try {
            SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);
        } catch (Exception e) {
            log.info("table schema element 없음");
            return currentBlocks;
        }
        resetTableSchemaSectionBlock(currentBlocks);
        return currentBlocks;
    }

    private List<LayoutBlock> handleTableChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        Integer selectTableNameBlockIndex = null;
        try {
            selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);
        } catch (Exception e) {
            log.info("테이블 최초 선택");
            return currentBlocks;
        }
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId);
        String selectedTableName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId);
        SectionBlock tableSchema = (SectionBlock) fetchTableSchemaBlocks(selectedDatabaseConnectionInfo, selectedSchemaName, selectedTableName).get(1);
        currentBlocks.set(selectTableNameBlockIndex, tableSchema);
        return currentBlocks;
    }

    private List<LayoutBlock> fetchTableSchemaBlocks(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        List<LayoutBlock> blocks = new ArrayList<>();
        String tableSchemaLabelText = "<Table Schema>";
        ContextBlock contextBlock = BasicBlockFactory.getContextBlock(tableSchemaLabelText, SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaContextId);
        blocks.add(contextBlock);

        String tableSchema = describeService.findTableSchema(databaseConnectionInfo, schemaName, tableName);
        SectionBlock textSection = BasicBlockFactory.getTextSection(tableSchema, SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);
        blocks.add(textSection);

        return blocks;
    }

    private void setSchemaNameOptions(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String DBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId);
        log.info("DBMSName: {}", DBMSName);
        DatabaseConnectionInfo databaseConnectionInfo = DynamicDataSourceProperties.findByDbName(DBMSName);
        List<OptionObject> schemaNameOptions = fetchSchemaNameOptions(databaseConnectionInfo);
        StaticSelectElement schemaSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId,
                schemaNameOptions,
                schemaPlaceholder);
        log.info("schemaSelectElement: {}", schemaSelectElement);
        int schemaSelectBlockIndex = SlackService.findBlockIndex(currentBlocks,
                "actions",
                SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId);
        ActionsBlock actionsBlock = (ActionsBlock) currentBlocks.get(schemaSelectBlockIndex);
        List<BlockElement> elements = actionsBlock.getElements();

        int schemaElementIndex = SlackService.findElementIndex(elements, SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId);
        elements.set(schemaElementIndex, schemaSelectElement);
        actionsBlock.setElements(elements);
        currentBlocks.set(schemaSelectBlockIndex, actionsBlock);
    }

    private static void resetTableSchemaSectionBlock(List<LayoutBlock> currentBlocks) {
        int tableSchemaTextSectionIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);
        SectionBlock textSection = BasicBlockFactory.getTextSection("choose table first", SlackConstants.CommandBlockIds.ClusterSchemaTable.tableSchemaTextId);
        currentBlocks.set(tableSchemaTextSectionIndex, textSection);
    }

    private void setTableNameOptions(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.ClusterSchemaTable.findSchemaSelectsElementActionId);
        List<OptionObject> tableNameOptions = fetchTableNameOptions(selectedDatabaseConnectionInfo, selectedSchemaName);

        int selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks,
                "actions",
                SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId);

        StaticSelectElement tableSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId,
                tableNameOptions,
                tablePlaceholder);
//
        ActionsBlock actionsBlock = (ActionsBlock) currentBlocks.get(selectTableNameBlockIndex);
        List<BlockElement> elements = actionsBlock.getElements();

        int tableElementIndex = SlackService.findElementIndex(elements, SlackConstants.CommandBlockIds.ClusterSchemaTable.findTableSelectsElementActionId);
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
