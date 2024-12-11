package zzangmin.db_automation.schedule.standardcheck;

import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.response.standardcheck.StandardCheckResultResponseDTO;
import zzangmin.db_automation.service.SlackMessageService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class StandardMonitor {

    private static final long STANDARD_CHECK_DELAY = 9999999999l;

    private final ClusterCreationStandardChecker clusterCreationStandardChecker;
    private final InstanceCreationStandardChecker instanceCreationStandardChecker;
    private final ParameterStandardChecker parameterStandardChecker;
    private final SchemaStandardChecker schemaStandardChecker;
    private final TagStandardChecker tagStandardChecker;
    private final AccountStandardChecker accountStandardChecker;
    private final PluginComponentStandardChecker pluginComponentStandardChecker;
    private final VariableStandardChecker variableStandardChecker;
    private final SlackMessageService slackMessageService;

    @Scheduled(fixedDelay = STANDARD_CHECK_DELAY)
    public void checkStandard() {
        List<LayoutBlock> resultBlocks = new ArrayList<>();
        List<StandardCheckResultResponseDTO> results = new ArrayList<>();
        log.info("standard check start");

        results.addAll(parameterStandardChecker.checkParameterStandard());
        results.addAll(clusterCreationStandardChecker.checkClusterCreationStandard());
        results.addAll(instanceCreationStandardChecker.checkInstanceCreationStandard());
        results.addAll(schemaStandardChecker.checkSchemaStandard());
        results.addAll(tagStandardChecker.checkTagStandard());
        results.addAll(accountStandardChecker.checkAccountStandard());
        results.addAll(pluginComponentStandardChecker.checkPluginComponentStandard());
        results.addAll(variableStandardChecker.checkVariableStandard());

        log.info("standard check finish");
        results.forEach(i -> resultBlocks.addAll(i.toSlackMessageBlock()));
        slackMessageService.sendBlockMessage(resultBlocks);
    }
}
