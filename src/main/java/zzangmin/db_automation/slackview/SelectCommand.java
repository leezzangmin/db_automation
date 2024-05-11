package zzangmin.db_automation.slackview;

import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.SlackController;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class SelectCommand {
    private final CreateIndexBlockPage createIndexBlockPage;
    private final CreateTableBlockPage createTableBlockPage;

    private static final String findCommandGroupPlaceholder = "select database command group";
    private static final String findCommandTypePlaceholder = "select database command type";

    public static List<LayoutBlock> selectCommandGroupAndCommandTypeBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        List<OptionObject> databaseRequestGroupOptions = Arrays.stream(DatabaseRequestCommandGroup.values())
                .map(group -> OptionObject.builder()
                        .text(plainText(group.name()))
                        .value(group.name())
                        .build()
                )
                .collect(Collectors.toList());
        StaticSelectElement commandGroupSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId,
                databaseRequestGroupOptions,
                findCommandGroupPlaceholder);

        List<OptionObject> commandTypeOptions = DatabaseRequestCommandGroup.findDatabaseRequestCommandTypes(DatabaseRequestCommandGroup.EMPTY)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        StaticSelectElement commandTypeSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId,
                commandTypeOptions,
                findCommandTypePlaceholder);

        ActionsBlock selectCommandGroupAndCommandTypeBlocks = actions(List.of(commandGroupSelectElement, commandTypeSelectElement));
        blocks.add(selectCommandGroupAndCommandTypeBlocks);
        return blocks;
    }

    public List<LayoutBlock> handleCommandGroupChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        int commandTypeBlockIndex = SlackService.findBlockIndex(currentBlocks,
                "actions",
                SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);
        ActionsBlock currentBlock = (ActionsBlock) currentBlocks.get(commandTypeBlockIndex);
        List<BlockElement> currentBlockElements = currentBlock.getElements();

        int commandTypeElementIndex = SlackService.findElementIndex(currentBlockElements, SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);

        String selectedDatabaseRequestGroupName = SlackService.findCurrentValueFromState(values, SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId);
        DatabaseRequestCommandGroup selectedDatabaseRequestGroup = findDatabaseRequestCommandGroupByName(selectedDatabaseRequestGroupName);
        List<OptionObject> commandTypeOptions = findDatabaseRequestCommandTypes(selectedDatabaseRequestGroup)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        StaticSelectElement commandTypeSelectElement = BasicBlockFactory.findStaticSelectsElement(SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId,
                commandTypeOptions,
                findCommandTypePlaceholder);
        currentBlockElements.set(commandTypeElementIndex, commandTypeSelectElement);
        currentBlock.setElements(currentBlockElements);
        currentBlocks.set(commandTypeBlockIndex, currentBlock);
        return currentBlocks;
    }

    public List<LayoutBlock> handleCommandTypeChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String selectedCommandTypeName = SlackService.findCurrentValueFromState(values, SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);
        DatabaseRequestCommandGroup.CommandType findCommandType = findCommandTypeByCommandTypeName(selectedCommandTypeName);
        currentBlocks.addAll(generateCommandTypeBlocks(findCommandType));

        return currentBlocks;
    }

    private List<LayoutBlock> generateCommandTypeBlocks(DatabaseRequestCommandGroup.CommandType commandType) {
        if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_INDEX)) {
            return createIndexBlockPage.createIndexBlocks();
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.CREATE_TABLE)) {
            return createTableBlockPage.createIndexBlocks();
        } else if (commandType.equals(DatabaseRequestCommandGroup.CommandType.ADD_COLUMN)) {
            // generate createaddcolumnblock and add to blocks
        }
        throw new IllegalArgumentException("미구현 commandType: " + commandType);
    }

}
