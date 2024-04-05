package zzangmin.db_automation.util;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.util.json.GsonFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlackMessageUtil {
    public static List<LayoutBlock> asBlocks(LayoutBlock blocks) {
        return Arrays.asList(blocks);
    }

    public HeaderBlock getHeader(String text) {
        return HeaderBlock.builder()
                .text(PlainTextObject.builder()
                        .text(text)
                        .emoji(true)
                        .build())
                .build();
    }

    public SectionBlock getSection(String message) {
        return SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(message)
                        .build())
                .build();
    }

    public InputBlock getTextArea(String labelText, String actionId){
        return InputBlock.builder()
                .element(PlainTextInputElement.builder()
                        .actionId(actionId)
                        .multiline(true)
                        .build())
                .label(PlainTextObject.builder()
                        .text(labelText)
                        .build())
                .blockId(actionId)
                .build();
    }
    public InputBlock getTextInput(String labelText, String actionId){
        return InputBlock.builder()
                .element(PlainTextInputElement.builder()
                        .actionId(actionId)
                        .build())
                .label(PlainTextObject.builder()
                        .text(labelText)
                        .build())
                .blockId(actionId)
                .build();
    }

    public List<BlockElement> getConfirmButtonBlocks() {
        List<BlockElement> actions = new ArrayList<>();
        actions.add(getActionButton("전송", "submit", "primary", "callback_submit"));
        return actions;
    }

    public BlockElement getActionButton(String plainText, String value, String style, String actionId) {
        return ButtonElement.builder()
                .text(PlainTextObject.builder()
                        .text(plainText)
                        .emoji(true)
                        .build())
                .value(value)
                .style(style)
                .actionId(actionId)
                .build();
    }

    public BlockActionPayload getPayload(String payloadJson){
        return GsonFactory.createSnakeCase().fromJson(payloadJson, BlockActionPayload.class);
    }

}
