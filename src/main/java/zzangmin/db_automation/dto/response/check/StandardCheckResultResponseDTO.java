package zzangmin.db_automation.dto.response.check;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class StandardCheckResultResponseDTO {
    private String accountId;
    private String instanceName;
    private StandardType standardType;
    private String standardName;
    // 표준값
    private String standardValue;
    // 설정되어있는값
    private String currentValue;
    private String errorDescription;

    public enum StandardType {
        ACCOUNT,
        CLUSTER_CREATION,
        INSTANCE_CREATION,
        PARAMETER,
        PLUGIN_COMPONENT,
        SCHEMA,
        TAG,
        VARIABLE
    }

    public List<LayoutBlock> toSlackMessageBlock() {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.add(HeaderBlock.builder()
                .text(PlainTextObject.builder()
                        .text("Standard Check Result")
                        .emoji(true)
                        .build())
                .build());

        List<TextObject> fields = new ArrayList<>();
        fields.add(MarkdownTextObject.builder().text("*Account ID:*\n" + (accountId != null ? accountId : "N/A")).build());
        fields.add(MarkdownTextObject.builder().text("*Instance:*\n" + instanceName).build());
        fields.add(MarkdownTextObject.builder().text("*Type:*\n" + standardType).build());
        fields.add(MarkdownTextObject.builder().text("*Standard Name:*\n" + standardName).build());
        fields.add(MarkdownTextObject.builder().text("*Standard Value:*\n" + (standardValue != null ? standardValue : "N/A")).build());
        fields.add(MarkdownTextObject.builder().text("*Current Value:*\n" + (currentValue != null ? currentValue : "N/A")).build());


        blocks.add(SectionBlock.builder()
                .fields(fields)
                .build());

        if (errorDescription != null && !errorDescription.isEmpty()) {
            blocks.add(ContextBlock.builder()
                    .elements(Collections.singletonList(
                            MarkdownTextObject.builder().text(":warning: " + errorDescription).build()
                    ))
                    .build());
        }

        // 구분선 추가
        blocks.add(DividerBlock.builder().build());

        return blocks;
    }
}
