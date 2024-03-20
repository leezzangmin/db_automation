package zzangmin.db_automation.schedule.standardcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.AwsService;

@RequiredArgsConstructor
@Component
public class SecretManagerChecker {

    private final AwsService awsService;
}
