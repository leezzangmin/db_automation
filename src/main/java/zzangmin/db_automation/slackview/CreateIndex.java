package zzangmin.db_automation.slackview;

import com.slack.api.model.block.*;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import java.util.List;

public class CreateIndex {




    public List<LayoutBlock> createIndexBlocks(DatabaseConnectionInfo databaseConnectionInfo) {
//        List<LayoutBlock> blocks = new ArrayList<>();
//
//        List<OptionObject> schemaNameOptions = fetchSchemaNames(databaseConnectionInfo)
//                .stream()
//                .map(schemaName -> OptionObject.builder()
//                        .value(schemaName)
//                        .text(plainText(schemaName))
//                        .build())
//                .collect(Collectors.toList());
//        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findSchemaSelectsElementActionId,
//                schemaNameOptions,
//                schemaPlaceholder));
//
//        blocks.add(BasicBlockFactory.findStaticSelectsBlock(SlackController.findTableSelectsElementActionId,
//                new ArrayList<>(),
//                tablePlaceholder));
//        return blocks;
        return null;
    }




}
