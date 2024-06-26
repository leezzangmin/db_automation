package zzangmin.db_automation.view;

import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.*;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;

import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;

public class BasicBlockFactory {

    public static View findGlobalRequestModalView(List<LayoutBlock> blocks) {
        return View.builder()
                .type("modal")
                .callbackId("global-request-modal")
                .title(ViewTitle.builder()
                        .type("plain_text")
                        .text("Database Request")
                        .emoji(true)
                        .build())
                .blocks(blocks)
                .submit(ViewSubmit.builder()
                        .type("plain_text")
                        .text("Database Request submit")
                        .emoji(true)
                        .build())
                .build();
    }

    public static HeaderBlock findHeaderBlock(String text, String blockId) {
        HeaderBlock headerBlock = HeaderBlock.builder()
                .text(plainText(text))
                .blockId(blockId)
                .build();
        return headerBlock;
    }

    public static InputBlock findSinglelinePlainTextInput(String id, String label, String placeholder) {
        return input(input -> input
                .element(plainTextInput(pti -> pti.actionId(id)
                        .multiline(false)
                        .placeholder(plainText(placeholder))
                )).optional(false)
                .label(plainText(label))
                .blockId(id));
    }

    public static InputBlock findSinglelinePlainTextOptionalInput(String id, String label, String placeholder) {
        return input(input -> input
                .element(plainTextInput(pti -> pti.actionId(id)
                        .multiline(false)
                        .placeholder(plainText(placeholder))
                )).optional(true)
                .label(plainText(label))
                .blockId(id));
    }


    public static InputBlock findMultilinePlainTextInput(String id, String label, String placeholder) {
        return input(input -> input
                .element(plainTextInput(pti -> pti.actionId(id)
                        .multiline(true)
                        .placeholder(plainText(placeholder))
                )).optional(false)
                .label(plainText(label))
                .blockId(id));
    }

    public static ActionsBlock findSubmitButton(String id, String text, String value) {
        return actions(actions -> actions
                .elements(asElements(
                        button(b -> b.text(plainText(pt -> pt.emoji(true).text("승인")))
                                .value(value)
                                .style("primary")
                                .text(plainText(text))
                                .actionId(id)
                        ))).blockId(id)
        );
    }

    public static SectionBlock findMultiSelectsBlock(String id, List<OptionObject> selectOptions, String label, String placeHolder) {
        MultiStaticSelectElement multiStaticSelectElement = MultiStaticSelectElement.builder()
                .options(selectOptions)
                .placeholder(plainText(placeHolder))
                .actionId(id)
                .build();
        return section(section -> section.accessory(multiStaticSelectElement)
                .text(plainText(label)));
    }

    public static List<OptionObject> findOptionObjects(List<String> options) {
        List<OptionObject> optionObjects = options.stream()
                .map(option -> OptionObject.builder()
                        .text(plainText(option))
                        .value(option)
                        .build()
                ).collect(Collectors.toList());
        return optionObjects;
    }

    public static ActionsBlock findStaticSelectsBlock(String id, List<OptionObject> selectOptions, String placeHolder) {
        return actions(actions -> actions.elements(asElements(StaticSelectElement.builder()
                        .options(selectOptions)
                        .placeholder(plainText(placeHolder))
                        .actionId(id)
                        .build()))
                .blockId(id));
    }

    public static StaticSelectElement findStaticSelectsElement(String id, List<OptionObject> selectOptions, String placeHolder) {
        return StaticSelectElement.builder()
                .options(selectOptions)
                .placeholder(plainText(placeHolder))
                .actionId(id)
                .build();

    }

    public static View findView(String viewId, String callbackId, String viewTitle, List<LayoutBlock> blocks, String submitText) {
        return View.builder()
                .id(viewId)
                .type("modal")
                .callbackId(callbackId)
                .title(ViewTitle.builder()
                        .type("plain_text")
                        .text(viewTitle)
                        .emoji(true)
                        .build())
                .blocks(blocks)
                .submit(ViewSubmit.builder()
                        .type("plain_text")
                        .text(submitText)
                        .emoji(true)
                        .build())
                .build();
    }

    public static SectionBlock getPlainTextSection(String text, String textSectionBlockId) {
        SectionBlock sectionBlock = section(section -> section.text(plainText(text))
                .blockId(textSectionBlockId));
        return sectionBlock;
    }

    public static SectionBlock getMarkdownTextSection(String text, String textSectionBlockId) {
        SectionBlock sectionBlock = section(section -> section.text(markdownText(text))
                .blockId(textSectionBlockId));
        return sectionBlock;
    }

    public static ContextBlock getContextBlock(String text, String contextId) {
        return ContextBlock.builder()
                .elements(List.of(plainText(text)))
                .blockId(contextId)
                .build();
    }

    public static SectionBlock getRadioBlock(List<OptionObject> options, String id, String text) {
        return section(section -> section.accessory(RadioButtonsElement.builder()
                        .options(options)
                        .actionId(id)
                        .build())
                .text(plainText(text))
                .blockId(id)
        );
    }

    /**
     * rich text는 developer에게 지원되지 않음
     */
    // https://github.com/slackapi/java-slack-sdk/issues/876
    // https://github.com/slackapi/java-slack-sdk/issues/736
//    public static RichTextBlock getRichText(String text, String richTextBlockId) {
//
//    }

    public static DividerBlock findDividerBlock() {
        DividerBlock divider = divider();
        return divider;
    }

    public static List<OptionObject> generateEmptyOptionObjects() {
        return List.of(OptionObject.builder()
                .text(plainText("empty option objects"))
                .value("dropdown option empty...")
                .build());
    }
}
