package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateTableBlockPage {

    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static String createTableSQLTextInputLabel = "Create Table SQL";
    private static String createTableSQLPlaceHolder = "create table ....";

//            "CREATE TABLE `sample` (\n" +
//            "  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '사용자 고유번호',\n" +
//            "  `user_name` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '국문 사용자 성명',\n" +
//            "  `user_name_en` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '영문 사용자 성명',\n" +
//            "  `nickname` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '닉네임',\n" +
//            "  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '이메일',\n" +
//            "  `created_by` bigint(20) NOT NULL COMMENT '등록자',\n" +
//            "  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',\n" +
//            "  `updated_by` bigint(20) DEFAULT NULL COMMENT '수정자',\n" +
//            "  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',\n" +
//            "  PRIMARY KEY (`user_id`),\n" +
//            "  UNIQUE KEY `uk_externaluserid` (`external_user_id`)\n" +
//            ") ENGINE=InnoDB AUTO_INCREMENT=123456 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테이블코멘트샘플'";

    public List<LayoutBlock> createIndexBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTable.selectClusterSchemaBlocks());

        blocks.add(BasicBlockFactory.findMultilinePlainTextInput(SlackConstants.CommandBlockIds.createTableSQLTextInputId,
                createTableSQLTextInputLabel,
                createTableSQLPlaceHolder));

        return blocks;
    }

    public void handleSubmission(List<LayoutBlock> currentBlocks, Map<String, Map<String, ViewState.Value>> values) {
        String createTableStatementSQL = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.createTableSQLTextInputId);
        log.info("createTableStatementSQL: {}", createTableStatementSQL);
        CreateTableRequestDTO createTableRequestDTO;
        try {
            createTableRequestDTO = CreateTableRequestDTO.of(createTableStatementSQL);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        log.info("createTableRequestDTO: {}", createTableStatementSQL);

        String selectedDBMSName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findClusterSelectsElementActionId);
        log.info("selectedDBMSName: {}", selectedDBMSName);
        DatabaseConnectionInfo selectedDatabaseConnectionInfo = DynamicDataSourceProperties.findByDbName(selectedDBMSName);
        log.info("selectedDatabaseConnectionInfo: {}", selectedDatabaseConnectionInfo);
        String schemaName = SlackService.findCurrentValueFromState(values, SlackConstants.CommandBlockIds.findSchemaSelectsElementActionId);
        log.info("schemaName: {}", schemaName);

        createTableRequestDTO.setCommandType(CommandType_old.CREATE_TABLE);
        createTableRequestDTO.setSchemaName(schemaName);
        ddlValidator.validateCreateTable(selectedDatabaseConnectionInfo, createTableRequestDTO);
        ddlController.createTable(selectedDatabaseConnectionInfo, createTableRequestDTO);
    }
}