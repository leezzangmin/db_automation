package zzangmin.db_automation.slackview;

import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
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

import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.findDatabaseRequestCommandGroupByName;
import static zzangmin.db_automation.entity.DatabaseRequestCommandGroup.findDatabaseRequestCommandTypes;

@Slf4j
@RequiredArgsConstructor
@Component
public class SelectCommand {


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
        ActionsBlock commandGroupSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackController.findDatabaseRequestCommandGroupSelectsElementActionId,
                databaseRequestGroupOptions,
                findCommandGroupPlaceholder);
        blocks.add(commandGroupSelectBlock);

        List<OptionObject> commandTypeOptions = DatabaseRequestCommandGroup.findDatabaseRequestCommandTypes(DatabaseRequestCommandGroup.EMPTY)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        ActionsBlock commandTypeSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackController.findCommandTypeSelectsElementActionId,
                commandTypeOptions,
                findCommandTypePlaceholder);
        blocks.add(commandTypeSelectBlock);

        return blocks;
    }

    public static List<LayoutBlock> handleCommandGroupChange(List<LayoutBlock> blocks, Map<String, Map<String, ViewState.Value>> values) {
        int commandTypeBlockIndex = SlackService.findBlockIndex(blocks,
                "actions",
                SlackController.findCommandTypeSelectsElementActionId);
        String selectedDatabaseRequestGroupName = SlackService.findCurrentValueFromState(values, SlackController.findDatabaseRequestCommandGroupSelectsElementActionId);
        DatabaseRequestCommandGroup selectedDatabaseRequestGroup = findDatabaseRequestCommandGroupByName(selectedDatabaseRequestGroupName);
        List<OptionObject> commandTypeOptions = findDatabaseRequestCommandTypes(selectedDatabaseRequestGroup)
                .stream()
                .map(commandType -> OptionObject.builder()
                        .text(plainText(commandType.name()))
                        .value(commandType.name())
                        .build()
                )
                .collect(Collectors.toList());
        ActionsBlock commandTypeSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackController.findCommandTypeSelectsElementActionId,
                commandTypeOptions,
                findCommandTypePlaceholder);
        blocks.set(commandTypeBlockIndex, commandTypeSelectBlock);
        return blocks;
    }

    public static List<LayoutBlock> handleCommandTypeChange() {
        return null;
    }
}
