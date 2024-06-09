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
import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeGrantRequestDTO;
import zzangmin.db_automation.dto.request.account.MysqlPrivilegeShowRequestDTO;
import zzangmin.db_automation.dto.response.account.MysqlPrivilegeGrantResponseDTO;
import zzangmin.db_automation.dto.response.account.MysqlPrivilegeResponseDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.BasicBlockFactory;
import zzangmin.db_automation.slackview.SlackConstants;
import zzangmin.db_automation.slackview.commandpage.BlockPage;
import zzangmin.db_automation.slackview.globalpage.SelectClusterSchemaTableBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class GrantBlockPage implements BlockPage {
    private final SelectClusterSchemaTableBlocks selectClusterSchemaTableBlocks;
    private final MysqlAccountController mysqlAccountController;

    private final String selectAccountPlaceholder = "Select account";
    private final String findAccountButtonText = "계정목록조회";
    private final String grantDCLInputLabel = "Privileges: ";
    private final String grantDCLPlaceholder = "SELECT, INSERT, DROP";
    private final String grantTargetInputLabel = "Target :";
    private final String grantTargetPlaceholder = "`test_db`.`test_table`";

    @Override
    public List<LayoutBlock> generateBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.addAll(selectClusterSchemaTableBlocks.selectClusterBlocks());

        // 계정 목록 조회 버튼
        ActionsBlock submitButton = BasicBlockFactory.findSubmitButton(SlackConstants.CommandBlockIds.Grant.grantFindAccountListButtonBlockId,
                findAccountButtonText,
                SlackConstants.CommandBlockIds.ShowGrant.showGrantFindAccountListButtonBlockId);
        blocks.add(submitButton);

        // 계정 선택
        List<OptionObject> accountNameEmptyOption = BasicBlockFactory.generateEmptyOptionObjects();
        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.Grant.grantSelectMysqlAccountSelectBlockId,
                accountNameEmptyOption,
                selectAccountPlaceholder));

        // grant input (SELECT, INSERT, DELETE, ..., etc)
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.Grant.grantPrivilegeInputId,
                grantDCLInputLabel,
                grantDCLPlaceholder));

        // target input (test_db.*)
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.Grant.grantTargetInputId,
                grantTargetInputLabel,
                grantTargetPlaceholder));
        return blocks;
    }

    @Override
    public RequestDTO handleSubmission(Map<String, Map<String, ViewState.Value>> values) {
        DatabaseConnectionInfo databaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);
        String accountName = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.Grant.grantSelectMysqlAccountSelectBlockId);
        List<String> privileges = Arrays.stream(SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.Grant.grantPrivilegeInputId).split(",")).collect(Collectors.toList());
        String target = SlackService.findCurrentValueFromState(values,
                SlackConstants.CommandBlockIds.Grant.grantTargetInputId);
        MysqlPrivilegeGrantRequestDTO mysqlPrivilegeGrantRequestDTO = new MysqlPrivilegeGrantRequestDTO(accountName, privileges, target);
        log.info("mysqlPrivilegeGrantRequestDTO: {}", mysqlPrivilegeGrantRequestDTO);

        mysqlAccountController.validateAccountRequest(databaseConnectionInfo, mysqlPrivilegeGrantRequestDTO);
        return mysqlPrivilegeGrantRequestDTO;
    }

    @Override
    public boolean supportsCommandType(DatabaseRequestCommandGroup.CommandType commandType) {
        return DatabaseRequestCommandGroup.CommandType.GRANT_PRIVILEGE.equals(commandType);
    }

    @Override
    public boolean supportsActionId(String actionId) {
        return SlackConstants.CommandBlockIds
                .getMembers(SlackConstants.CommandBlockIds.Grant.class)
                .contains(actionId);
    }

    @Override
    public void handleAction(String actionId, List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        if (actionId.equals(SlackConstants.CommandBlockIds.Grant.grantFindAccountListButtonBlockId)) {
            DatabaseConnectionInfo selectedDatabaseConnectionInfo = selectClusterSchemaTableBlocks.findDatabaseConnectionInfo(values);

            List<String> accountNames = mysqlAccountController.findAccountNames(selectedDatabaseConnectionInfo);
            List<OptionObject> accountNameOptions = BasicBlockFactory.findOptionObjects(accountNames);
            ActionsBlock accountSelectBlock = BasicBlockFactory.findStaticSelectsBlock(SlackConstants.CommandBlockIds.Grant.grantSelectMysqlAccountSelectBlockId,
                    accountNameOptions,
                    selectAccountPlaceholder);
            int selectAccountBlockIndex = SlackService.findBlockIndex(currentBlocks,
                    "actions",
                    SlackConstants.CommandBlockIds.Grant.grantSelectMysqlAccountSelectBlockId);

            currentBlocks.set(selectAccountBlockIndex, accountSelectBlock);
        } else {
            throw new IllegalArgumentException("GrantBlock Invalid actionId: " + actionId);
        }
    }

    @Override
    public List<LayoutBlock> generateRequestMessageBlocks(RequestDTO requestDTO) {
        List<LayoutBlock> blocks = new ArrayList<>();
        MysqlPrivilegeGrantRequestDTO mysqlPrivilegeGrantRequestDTO = (MysqlPrivilegeGrantRequestDTO) requestDTO;


        blocks.add(BasicBlockFactory.getMarkdownTextSection("*Request Content:* `" + mysqlPrivilegeGrantRequestDTO.toSQL() + "`", "GrantBlockPage"));

        return blocks;
    }

    @Override
    public String execute(DatabaseConnectionInfo databaseConnectionInfo, RequestDTO requestDTO, String slackUserId) {
        MysqlPrivilegeGrantResponseDTO mysqlPrivilegeGrantResponseDTO = mysqlAccountController.grantAccountPrivilege(databaseConnectionInfo, (MysqlPrivilegeGrantRequestDTO) requestDTO, slackUserId);
        return mysqlPrivilegeGrantResponseDTO.toString();
    }
}
