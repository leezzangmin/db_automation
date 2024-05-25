package zzangmin.db_automation.slackview.accountpage;

import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.MysqlAccountController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.response.MysqlPrivilegeResponseDTO;
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
    private final SlackService slackService;

    private final String selectAccountPlaceholder = "select account";
    private final String findAccountButtonText = "계정목록조회";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterBlocks());

        // 계정 선택
        List<OptionObject> accountNameEmptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.ShowGrant.selectMysqlAccountSelectBlockId,
                accountNameEmptyOption,
                selectAccountPlaceholder));

        // 계정 목록 조회 버튼
        ActionsBlock submitButton = BasicBlockFactory.findSubmitButton(SlackConstants.CommandBlockIds.ShowGrant.findAccountListButtonBlockId,
                findAccountButtonText,
                SlackConstants.CommandBlockIds.ShowGrant.findAccountListButtonBlockId);
        blocks.add(submitButton);

        return blocks;
    }

    @Override
    public void handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values, ViewSubmissionPayload.User slackUser) {
        List<LayoutBlock> resultMessageBlocks = new ArrayList<>();

        try {
            String accountName = SlackService.findCurrentValueFromState(values,
                    SlackConstants.CommandBlockIds.ShowGrant.selectMysqlAccountSelectBlockId);
            DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.getDatabaseConnectionInfo(values);
            MysqlPrivilegeResponseDTO mysqlPrivilegeResponseDTO = mysqlAccountController.findAccountPrivilege(selectedDatabaseConnectionInfo, accountName, slackUser);
            log.info("ShowGrant Account: {}", mysqlPrivilegeResponseDTO);

            resultMessageBlocks.add(Blocks.section(section -> section.text(BlockCompositions.plainText("Account Name: " + mysqlPrivilegeResponseDTO.getAccountName()))));
            for (String privilege : mysqlPrivilegeResponseDTO.getPrivileges()) {
                resultMessageBlocks.add(Blocks.section(section -> section.text(BlockCompositions.markdownText("```" + privilege + "```"))));
            }
            //slackService.sendBlockMessage(resultMessageBlocks);
        } catch (Exception e) {
            log.info("e: {}", e.getMessage());
            e.printStackTrace();
            return;
        }


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
        if (actionId.equals(SlackConstants.CommandBlockIds.ShowGrant.findAccountListButtonBlockId)) {
            DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.getDatabaseConnectionInfo(values);

            List<String> accountNames = mysqlAccountController.findAccountNames(selectedDatabaseConnectionInfo);
            List<OptionObject> accountNameOptions = BasicBlockFactory.findOptionObjects(accountNames);
            ActionsBlock accountSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.ShowGrant.selectMysqlAccountSelectBlockId,
                    accountNameOptions,
                    selectAccountPlaceholder);
            int selectAccountBlockIndex = SlackService.findBlockIndex(currentBlocks,
                    "actions",
                    SlackConstants.CommandBlockIds.ShowGrant.selectMysqlAccountSelectBlockId);

            currentBlocks.set(selectAccountBlockIndex, accountSelectBlock);
        }
    }
}

