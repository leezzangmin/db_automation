package zzangmin.db_automation.slackview.accountpage;

import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.MysqlAccountController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeShowRequestDTO;
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.response.account.MysqlPrivilegeResponseDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.slackview.commandpage.BlockPage;
import zzangmin.db_automation.slackview.globalpage.SelectClusterSchemaTableBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShowGrantBlockPage implements BlockPage {

    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final MysqlAccountController mysqlAccountController;

    private final String selectAccountPlaceholder = "select account";
    private final String findAccountButtonText = "계정목록조회";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterBlocks());

        // 계정 목록 조회 버튼
        ActionsBlock submitButton = BasicBlockFactory.findSubmitButton(SlackConstants.CommandBlockIds.ShowGrant.showGrantFindAccountListButtonBlockId,
                findAccountButtonText,
                SlackConstants.CommandBlockIds.ShowGrant.showGrantFindAccountListButtonBlockId);
        blocks.add(submitButton);


        // 계정 선택
        List<OptionObject> accountNameEmptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.ShowGrant.showGrantSelectMysqlAccountSelectBlockId,
                accountNameEmptyOption,
                selectAccountPlaceholder));

        return blocks;
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {
        String accountName = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.ShowGrant.showGrantSelectMysqlAccountSelectBlockId);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);

        MysqlPrivilegeShowRequestDTO mysqlPrivilegeShowRequestDTO = new MysqlPrivilegeShowRequestDTO(accountName);
        log.info("mysqlPrivilegeRequestDTO: {}", mysqlPrivilegeShowRequestDTO);

        mysqlAccountController.validateAccountRequest(selectedDatabaseConnectionInfo, mysqlPrivilegeShowRequestDTO);
        return mysqlPrivilegeShowRequestDTO;
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return commandType.equals(DatabaseRequestCommandGroup.CommandType.SHOW_GRANTS);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.ShowGrant.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        if (actionId.equals(SlackConstants.CommandBlockIds.ShowGrant.showGrantFindAccountListButtonBlockId)) {
            DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);

            List<String> accountNames = mysqlAccountController.findAccountNames(selectedDatabaseConnectionInfo);
            List<OptionObject> accountNameOptions = BasicBlockFactory.findOptionObjects(accountNames);
            ActionsBlock accountSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.ShowGrant.showGrantSelectMysqlAccountSelectBlockId,
                    accountNameOptions,
                    selectAccountPlaceholder);
            int selectAccountBlockIndex = SlackService.findBlockIndex(currentBlocks,
                    "actions",
                    SlackConstants.CommandBlockIds.ShowGrant.showGrantSelectMysqlAccountSelectBlockId);

            currentBlocks.set(selectAccountBlockIndex, accountSelectBlock);
        }
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        List<LayoutBlock> blocks = new ArrayList<>();
        MysqlPrivilegeShowRequestDTO mysqlPrivilegeShowRequestDTO = (MysqlPrivilegeShowRequestDTO) requestDTO;


        blocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Content:* `" + mysqlPrivilegeShowRequestDTO.getAccountName() + "`", "RenameTableBlockPage"));

        return blocks;
    }

    @Override
    public String execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        MysqlPrivilegeResponseDTO mysqlPrivilegeResponseDTO = mysqlAccountController.findAccountPrivilege(databaseConnectionInfo, (MysqlPrivilegeShowRequestDTO) requestDTO, slackUserId);
        return mysqlPrivilegeResponseDTO.toString();
    }
}

