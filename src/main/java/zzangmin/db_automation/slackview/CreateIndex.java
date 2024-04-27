package zzangmin.db_automation.slackview;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateIndex {
    private final DescribeService describeService;
    private final DynamicDataSourceProperties dataSourceProperties;

    private final String schemaPlaceholder = "select schema";
    private final String tablePlaceholder = "select table";

    public List<LayoutBlock> createIndexBlocks(DatabaseConnectionInfo databaseConnectionInfo) {
        List<LayoutBlock> blocks = new ArrayList<>();

        List<OptionObject> schemaNameOptions = fetchSchemaNames(databaseConnectionInfo)
                .stream()
                .map(schemaName -> OptionObject.builder()
                        .value(schemaName)
                        .text(plainText(schemaName))
                        .build())
                .collect(Collectors.toList());
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findSchemaSelectsElementActionId,
                schemaNameOptions,
                schemaPlaceholder));

        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findTableSelectsElementActionId,
                new ArrayList<>(),
                tablePlaceholder));
        return blocks;
    }


    private List<String> fetchTableNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        return describeService.findTableNames(databaseConnectionInfo, schemaName)
                .getTableNames().stream().toList();
    }

    private List<String> fetchSchemaNames(DatabaseConnectionInfo databaseConnectionInfo) {
        return new ArrayList<>(describeService.findSchemaNames(databaseConnectionInfo).getSchemaNames());
    }

    public List<LayoutBlock> fetchTableSchemaBlocks(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        List<LayoutBlock> blocks = new ArrayList<>();
        String tableSchemaLabelText = "<Table Schema>";
        InputBlock labelBLock = BasicBlockFactory.getLabelBLock(tableSchemaLabelText, SlackController.tableSchemaLabelId);
        blocks.add(labelBLock);

        String tableSchema = describeService.findTableSchema(databaseConnectionInfo, schemaName, tableName);
        SectionBlock textSection = BasicBlockFactory.getTextSection(tableSchema, SlackController.tableSchemaTextId);
        blocks.add(textSection);

        return blocks;
    }

    public List<LayoutBlock> handleSchemaChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        int selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks, "actions", SlackController.findTableSelectsElementActionId);
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = dataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackController.findSchemaSelectsElementActionId);

        List<OptionObject> tableNameOptions = fetchTableNames(selectedDatabaseConnectionInfo, selectedSchemaName)
                .stream()
                .map(tableName -> OptionObject.builder()
                        .value(tableName)
                        .text(plainText(tableName))
                        .build())
                .collect(Collectors.toList());
        ActionsBlock tableSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackController.findTableSelectsElementActionId, tableNameOptions, tablePlaceholder);
        currentBlocks.set(selectTableNameBlockIndex, tableSelectBlock);

        return currentBlocks;
    }

    public List<LayoutBlock> handleTableChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        Integer selectTableNameBlockIndex = null;
        try {
             selectTableNameBlockIndex = SlackService.findBlockIndex(currentBlocks, "section", SlackController.tableSchemaTextId);
        } catch (Exception e) {
            log.info("테이블 최초 선택");
        }
        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackController.findClusterSelectsElementActionId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = dataSourceProperties.findByDbName(selectedDBMSName);
        String selectedSchemaName = SlackService.findCurrentValueFromState(values, SlackController.findSchemaSelectsElementActionId);
        String selectedTableName = SlackService.findCurrentValueFromState(values, SlackController.findTableSelectsElementActionId);
        currentBlocks.set(selectTableNameBlockIndex, fetchTableSchemaBlocks(selectedDatabaseConnectionInfo, selectedSchemaName, selectedTableName).get(1));
        return currentBlocks;
    }

}
