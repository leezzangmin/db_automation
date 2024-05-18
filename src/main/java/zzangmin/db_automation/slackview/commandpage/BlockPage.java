package zzangmin.db_automation.slackview.commandpage;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;
import java.util.Map;

public interface BlockPage {

    List<LayoutBlock> generateBlocks();
    void handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values);
    boolean supports(DatabaseRequestCommandGroup.CommandType commandType);
}
