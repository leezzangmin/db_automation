package zzangmin.db_automation.slackview;

import com.slack.api.model.block.LayoutBlock;

import java.util.List;

public interface CommandTypeBlockGenerator {
    List<LayoutBlock> generateBlocks();
}
