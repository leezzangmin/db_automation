package zzangmin.db_automation.schedule.dbrequestexecutor;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DatabaseRequestExecutor {

    private static final int EXECUTE_DELAY = 10000;

    @Scheduled(fixedDelay = EXECUTE_DELAY)
    public void execute() {

    }
}
