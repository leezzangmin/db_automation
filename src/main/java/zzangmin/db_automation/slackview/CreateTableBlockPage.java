package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateTableBlockPage {

    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static String createTableTableNameTextInputLabel = "Table Name";
    private static String createTableNamePlaceHolder = "input table name";
    private static String createTableColumnNameTextInputLabel = "Column Name";
    private static String createTableColumnNamePlaceHolder = "input column name";
    private static String createTableColumnTypeTextInputLabel = "Column Type";
    private static String createTableColumnTypePlaceHolder = "input column type";
    private static String createTableColumnOptionTypeLabel = "Column Option";
    private static String createTableColumnOptionPlaceholder = "select column options from the list";
    private static String createTableColumnDefaultValueLabel = "Default Value";
    private static String createTableColumnDefaultValuePlaceholder = "input column default value";

    public List<LayoutBlock> createIndexBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTable.selectClusterSchemaBlocks());

        // 테이블 명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.createTableTableNameTextInputId,
                createTableTableNameTextInputLabel,
                createTableNamePlaceHolder));

        // 컬럼명, 컬럼타입, 컬럼옵션
        blocks.addAll(getInitialColumnBlocks());


        return blocks;
    }

    private List<LayoutBlock> getInitialColumnBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        // 컬럼 명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.createTableColumnNameTextInputId + 1,
                createTableColumnNameTextInputLabel + 1,
                createTableColumnNamePlaceHolder + 1));
        // 컬럼 타입
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.createTableColumnTypeTextInputId + 1,
                createTableColumnTypeTextInputLabel + 1,
                createTableColumnTypePlaceHolder + 1));

        // null, unique, auto_increment, pk
        blocks.add(BasicBlockFactory.findMultiSelectsBlock(SlackConstants.CommandBlockIds.createTableColumnOptionTypeMultiSelectSectionId + 1,
                getTableColumnOptionType(),
                createTableColumnOptionTypeLabel + 1,
                createTableColumnOptionPlaceholder + 1
        ));

        // default value
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.createTableColumnDefaultValueTextInputId + 1,
                createTableColumnDefaultValueLabel + 1,
                createTableColumnDefaultValuePlaceholder + 1));

        return blocks;
    }

    private List<OptionObject> getTableColumnOptionType() {
        List<OptionObject> optionObjects = new ArrayList<>();

        optionObjects.add(OptionObject.builder()
                .text(plainText("null"))
                .value("null")
                .build());
        optionObjects.add(OptionObject.builder()
                .text(plainText("unique"))
                .value("unique")
                .build());
        optionObjects.add(OptionObject.builder()
                .text(plainText("auto_increment"))
                .value("auto_increment")
                .build());
        optionObjects.add(OptionObject.builder()
                .text(plainText("primary key"))
                .value("primary key")
                .build());
        return optionObjects;
    }

    public void handleAddTableColumn() {

    }

    public void handleRemoveTableColumn() {

    }

    public void handleAddConstraint() {

    }

    public void handleAddConstraintColumn() {

    }

    public void handleRemoveConstraint() {

    }

    public void handleRemoveConstraintColumn() {

    }
}