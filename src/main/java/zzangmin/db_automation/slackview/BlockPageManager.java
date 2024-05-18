package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.slackview.commandpage.BlockPage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BlockPageManager {
    private final List<BlockPage> blockPages;

    @Autowired
    public BlockPageManager(List<BlockPage> blockPages) {
        this.blockPages = blockPages;
    }

    public List<LayoutBlock> generateBlocks(DatabaseRequestCommandGroup.CommandType commandType) {
        List<BlockPage> filteredBlockPages = this.blockPages.stream()
                .filter(h -> h.supports(commandType))
                .collect(Collectors.toList());

        if (filteredBlockPages.size() != 1) {
            throw new IllegalArgumentException("Unsupported or ambiguous command type: " + filteredBlockPages);
        }

        BlockPage selectedBlockPage = filteredBlockPages.get(0);

        return selectedBlockPage.generateBlocks();
    }

    public void handleSubmission(DatabaseRequestCommandGroup.CommandType commandType,
                              List<LayoutBlock> blocks, Map<String, Map<String, ViewState.Value>> values) {
        List<BlockPage> filteredBlockPages = this.blockPages.stream()
                .filter(h -> h.supports(commandType))
                .collect(Collectors.toList());

        if (filteredBlockPages.size() != 1) {
            throw new IllegalArgumentException("Unsupported or ambiguous command type: " + filteredBlockPages);
        }

        BlockPage selectedBlockPage = filteredBlockPages.get(0);

        selectedBlockPage.handleSubmission(blocks, values);
    }

}
