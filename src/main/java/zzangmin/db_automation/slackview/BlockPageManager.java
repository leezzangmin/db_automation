package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BlockPageManager {
    private final List<BlockPage> blockPages;

    @Autowired
    public BlockPageManager(@Lazy List<BlockPage> blockPages) {
        this.blockPages = blockPages;
    }

    public List<LayoutBlock> generateBlocks(DatabaseRequestCommandGroup.CommandType commandType) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);

        return selectedBlockPage.generateBlocks();
    }

    public void validateRequest() {
        // TODO
    }

    public RequestDTO handleSubmission(DatabaseRequestCommandGroup.CommandType commandType,
                                       Map<String, Map<String, ViewState.Value>> values) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);

        return selectedBlockPage.handleSubmission(values);
    }

    public String execute(DatabaseRequestCommandGroup.CommandType commandType,
                        DatabaseConnectionInfo databaseConnectionInfo,
                        RequestDTO requestDTO,
                        String slackUserId) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);
        String executeResult = selectedBlockPage.execute(databaseConnectionInfo, requestDTO, slackUserId);
        return executeResult;
    }

    public List<LayoutBlock> handleSubmissionRequestMessage(DatabaseRequestCommandGroup.CommandType commandType,
                                                            RequestDTO requestDTO) {
        BlockPage selectedBlockPage = findBlockPageByCommandType(commandType);

        return selectedBlockPage.generateRequestMessageBlocks(requestDTO);
    }

    public void handleAction(String actionId, List<LayoutBlock> blocks, Map<String, Map<String, ViewState.Value>> values) {
        BlockPage selectedBlockPage = findBlockPageByActionId(actionId);
        selectedBlockPage.handleAction(actionId, blocks, values);
    }

    private BlockPage findBlockPageByCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        List<BlockPage> filteredBlockPages = this.blockPages.stream()
                .filter(h -> h.supportsCommandType(commandType))
                .collect(Collectors.toList());
        if (filteredBlockPages.size() != 1) {
            throw new IllegalArgumentException("Unsupported or ambiguous command type: " + commandType);
        }
        BlockPage selectedBlockPage = filteredBlockPages.get(0);
        return selectedBlockPage;
    }

    private BlockPage findBlockPageByActionId(String actionId) {
        List<BlockPage> filteredBlockPages = this.blockPages.stream()
                .filter(h -> h.supportsActionId(actionId))
                .collect(Collectors.toList());
        if (filteredBlockPages.size() != 1) {
            throw new IllegalArgumentException("Unsupported or ambiguous actionId: " + actionId);
        }
        BlockPage selectedBlockPage = filteredBlockPages.get(0);
        return selectedBlockPage;
    }

}
