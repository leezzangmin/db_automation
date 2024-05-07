package zzangmin.db_automation.slackview;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.SlackController;
import zzangmin.db_automation.entity.Constraint;

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
//        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findIndexTypeActionId,
//                indexTypeOptions,
//                createIndexTypePlaceHolder));
//        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexIndexNameTextInputId,
//                createIndexIndexNameTextInputLabelId,
//                createIndexNamePlaceHolder));
//        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackController.createIndexColumnNameTextInputId,
//                SlackController.createIndexColumnNameTextInputId,
//                createIndexColumnPlaceHolder));
        ActionsBlock createIndexActionBlocks = actions(actions -> actions
                .elements(asElements(
                        BasicBlockFactory.findStaticSelectsElement(SlackController.findIndexTypeActionId,
                                indexTypeOptions,
                                createIndexTypePlaceHolder),
                        BasicBlockFactory.findSinglelinePlainTextInputElement(SlackController.createIndexIndexNameTextInputId,
                                createIndexIndexNameTextInputLabelId,
                                createIndexNamePlaceHolder),
                        BasicBlockFactory.findSinglelinePlainTextInputElement(SlackController.createIndexColumnNameTextInputId,
                                SlackController.createIndexColumnNameTextInputId,
                                createIndexColumnPlaceHolder)
                ))
        );
        blocks.add(createIndexActionBlocks);
        int i=0;
        for (LayoutBlock block : blocks) {
            System.out.println("i = " + i);
            i++;
            System.out.println("block = " + block);
        }
        return blocks;
    }

    public List<LayoutBlock> handleAddColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return null;
    }

    public List<LayoutBlock> handleRemoveColumn(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        return null;
    }

    public List<LayoutBlock> handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        List<String> columnNames = new ArrayList<>();
        String indexName = "indexName";

        return null;
    }




}
