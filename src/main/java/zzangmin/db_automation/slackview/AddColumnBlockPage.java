package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.controller.DDLController;
import zzangmin.db_automation.validator.DDLValidator;

import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@RequiredArgsConstructor
@Component
public class AddColumnBlockPage {

    private final SelectClusterSchemaTable selectClusterSchemaTable;
    private final DDLController ddlController;
    private final DDLValidator ddlValidator;

    private static final String columnNameLabel = "input column name";
    private static final String columnNamePlaceholder = "column_name";
    private static final String columnTypeLabel = "input column type";
    private static final String columnTypePlaceholder = "varchar(255)";
    private static final String columnCommentLabel = "input column comment";
    private static final String columnCommentPlaceholder = "column comment";
    private static final String columnIsNullLabel = "choose column nullable";
    private static final String columnDefaultValueLabel = "input column default value";
    private static final String columnDefaultValuePlaceholder = "12345";

    /**
     *  auto_increment, unique 컬럼 추가는 미지원
     *  -> [컬럼 추가 후 제약조건 추가] 2단계로 진행
     */
    public List<LayoutBlock> addColumnBlocks() {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.addAll(selectClusterSchemaTable.selectClusterSchemaTableBlocks());

        // 컬럼명
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.addColumnColumnNameTextInputId,
                columnNameLabel,
                columnNamePlaceholder));

        // 컬럼 타입
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.addColumnColumnTypeTextInputId,
                columnTypeLabel,
                columnTypePlaceholder));

        // nullable
        List<OptionObject> options = new ArrayList<>();
        options.add(OptionObject.builder()
                        .text(plainText("true"))
                        .value("NULL")
                        .build());
        options.add(OptionObject.builder()
                .text(plainText("false"))
                .value("NOT NULL")
                .build());
        blocks.add(BasicBlockFactory.getRadioBlock(options,
                SlackConstants.CommandBlockIds.addColumnColumnIsNullRadioId,
                columnIsNullLabel));

        // default value
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.addColumnColumnDefaultValueTextInputId,
                columnDefaultValueLabel,
                columnDefaultValuePlaceholder));

        // 코멘트
        blocks.add(BasicBlockFactory.findSinglelinePlainTextInput(SlackConstants.CommandBlockIds.addColumnColumnCommentTextInputId,
                columnCommentLabel,
                columnCommentPlaceholder));

// alter table table123 add column asdf123 varchar(100) null default 'asdf' unique comment 'asdf';

        return blocks;
    }
}
