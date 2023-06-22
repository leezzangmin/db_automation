package zzangmin.db_automation.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.service.AwsService;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RdsMetricValidator {
    private final static Map<String, Double> METRIC_THRESHOLD = Map.of("cpuUsage", 70.0, "memoryUsage", 30.0);
    private final AwsService awsService;

    public void validateMetricStable(String databaseIdentifier) {
        Map<String, Double> rdsPeakCpuAndMemoryUsage = awsService.findRdsPeakCpuAndMemoryUsage(databaseIdentifier);
        for (String metricName : rdsPeakCpuAndMemoryUsage.keySet()) {
            Double currentMetric = rdsPeakCpuAndMemoryUsage.get(metricName);
            Double standardMetric = METRIC_THRESHOLD.get(metricName);
            if (currentMetric > standardMetric) {
                throw new IllegalStateException(metricName + " 의 수치가 기준값보다 높아 실행이 불가능합니다. 현재값: " + currentMetric);
            }
        }
    }

}
