package zzangmin.db_automation.slackview;

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
    String execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId);
}
