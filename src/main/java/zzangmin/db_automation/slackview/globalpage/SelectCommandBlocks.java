package zzangmin.db_automation.slackview.globalpage;

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.*;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.BlockPageManager;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.slackview.commandpage.BlockPage;

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
public class SelectCommandBlocks implements BlockPage {

    private final BlockPageManager blockPageManager;

    private static final String findCommandGroupPlaceholder = "select database command group";
    private static final String findCommandTypePlaceholder = "select database command type";

    @Override
    public List<LayoutBlock> generateBlocks() {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public boolean supportsCommandType(CommandType commandType) {
        return false;
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.FixedBlockIds
                .isMember(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        if (actionId.equals(SlackConstants.FixedBlockIds.findDatabaseRequestCommandGroupSelectsElementActionId)) {
            handleCommandGroupChange(currentBlocks, values);
        } else if (actionId.equals(SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId)) {
            handleCommandTypeChange(currentBlocks, values);
        } else {
            throw new IllegalArgumentException("미지원 actionId: " + actionId);
        }
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        throw new IllegalArgumentException("미지원 page");
    }

    @Override
    public void execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        throw new IllegalArgumentException("미지원 page");

    }

    public static List<LayoutBlock> selectCommandGroupAndCommandTypeBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        // errorMessageBlock
        blocks.add(BasicBlockFactory.findSinglelinePlainTextOptionalInput(SlackConstants.ErrorBlockIds.errorMessageBlockId, "에러 메세지 표시용 블럭", "사용되지 않는 블럭입니다."));

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

    private List<LayoutBlock> handleCommandGroupChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
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

    private List<LayoutBlock> handleCommandTypeChange(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String selectedCommandTypeName = SlackService.findCurrentValueFromState(values, SlackConstants.FixedBlockIds.findCommandTypeSelectsElementActionId);
        DatabaseRequestCommandGroup.CommandType findCommandType = findCommandTypeByCommandTypeName(selectedCommandTypeName);
        removeCommandBlocks(currentBlocks);
        currentBlocks.addAll(blockPageManager.generateBlocks(findCommandType));

        return currentBlocks;
    }

    private void removeCommandBlocks(List<LayoutBlock> currentBlocks) {
        for (LayoutBlock currentBlock : currentBlocks) {
            log.info("currentBlockId: {}", currentBlock.getBlockId());
        }
        List<LayoutBlock> commandBlocks = new ArrayList<>();
        for (int i = 0;i < currentBlocks.size();i++) {
            LayoutBlock currentBlock = currentBlocks.get(i);
            if (SlackConstants.CommandBlockIds.isMember(currentBlock.getBlockId())) {
                commandBlocks.add(currentBlock);
                continue;
            }

            // actions 블록의 내부 element 검사
            if (currentBlock instanceof ActionsBlock) {
                ActionsBlock currentActionsBlock = (ActionsBlock) currentBlock;
                List<BlockElement> elements = currentActionsBlock.getElements();
                for (int j = 0;j < elements.size();j++) {
                    BlockElement blockElement = elements.get(j);
                    log.info("blockElement: {}", blockElement);
                    if (blockElement instanceof PlainTextInputElement) {
                        PlainTextInputElement childElement = (PlainTextInputElement) blockElement;
                        if (SlackConstants.CommandBlockIds.isMember(childElement.getActionId())) {
                            commandBlocks.add(currentActionsBlock);
                            break;
                        }
                    } else if (blockElement instanceof StaticSelectElement) {
                        StaticSelectElement childElement = (StaticSelectElement) blockElement;
                        if (SlackConstants.CommandBlockIds.isMember(childElement.getActionId())) {
                            commandBlocks.add(currentActionsBlock);
                            break;
                        }
                    } else if (blockElement instanceof ButtonElement) {
                        ButtonElement childElement = (ButtonElement) blockElement;
                        if (SlackConstants.CommandBlockIds.isMember(childElement.getActionId())) {
                            commandBlocks.add(currentActionsBlock);
                            break;
                        }
                    } else {
                        log.error("blockElement: {}", blockElement);
                        throw new IllegalStateException("미지원 Element Type. 구현을 추가해야 합니다.");
                    }
                }
            } else if (currentBlock instanceof SectionBlock) {
                SectionBlock currentSectionBlock = (SectionBlock) currentBlock;
                if (SlackConstants.CommandBlockIds.isMember(currentSectionBlock.getBlockId())) {
                    commandBlocks.add(currentBlock);
                    continue;
                }
                BlockElement blockElement = currentSectionBlock.getAccessory();
                if (blockElement instanceof MultiStaticSelectElement) {
                    MultiStaticSelectElement childElement = (MultiStaticSelectElement) blockElement;
                    if (SlackConstants.CommandBlockIds.isMember(childElement.getActionId())) {
                        commandBlocks.add(currentBlock);
                    } else {
                        log.error("blockElement: {}", blockElement);
                        throw new IllegalStateException("미지원 Element Type. 구현을 추가해야 합니다.");
                    }
                }

                // ContextBlock 의 Element는 id가 없음
            } else if (currentBlock instanceof ContextBlock) {
                ContextBlock currentContextBlock = (ContextBlock) currentBlock;
                if (SlackConstants.CommandBlockIds.isMember(currentContextBlock.getBlockId())) {
                    commandBlocks.add(currentBlock);
                    continue;
                }
            } else if (currentBlock instanceof InputBlock) {
                InputBlock currentInputBlock = (InputBlock) currentBlock;
                if (SlackConstants.CommandBlockIds.isMember(currentInputBlock.getBlockId())) {
                    commandBlocks.add(currentBlock);
                    continue;
                }
            }
            else {
                log.error("currentBlock: {}", currentBlock);
                throw new IllegalStateException("미지원 Block Type. 구현을 추가해야 합니다.");
            }
        }

        currentBlocks.removeAll(commandBlocks);
        for (LayoutBlock currentBlock : currentBlocks) {
            log.info("222currentBlockId: {}", currentBlock.getBlockId());
        }
    }
}
