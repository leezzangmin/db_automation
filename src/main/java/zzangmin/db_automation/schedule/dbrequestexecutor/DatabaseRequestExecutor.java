package zzangmin.db_automation.schedule.dbrequestexecutor;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.view.BlockPageManager;

@RequiredArgsConstructor
@Component
public class DatabaseRequestExecutor {

    private static final int EXECUTE_DELAY = 10000;

    private final BlockPageManager blockPageManager;

    @Scheduled(fixedDelay = EXECUTE_DELAY)
    public void execute() {

    }
}
