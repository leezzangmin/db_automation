package zzangmin.db_automation.slackview.commandpage;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.model.User;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;
import java.util.Map;

public interface BlockPage {

    List<LayoutBlock> generateBlocks();
    RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values);
    boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType);
    boolean supportsActionId(String actionId);
    void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values);
    List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO);
    void execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId);
}
