package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import zzangmin.db_automation.client.AwsClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Profile(value = "deploy")
@Service
public class AwsServiceDeploy implements AwsService {

    private final AwsClient awsClient;
    private final int DURATION_MINUTE = 5;

    @Override
    public Map<String, Double> findRdsPeakCpuAndMemoryUsage(String databaseIdentifier) {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMinutes(DURATION_MINUTE));

        CloudWatchClient cloudWatchClient = awsClient.getCloudWatchClient();
        GetMetricDataRequest metricDataRequest = GetMetricDataRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .scanBy(ScanBy.TIMESTAMP_ASCENDING)
                .metricDataQueries(
                        MetricDataQuery.builder()
                                .id("cpuUsage")
                                .metricStat(MetricStat.builder()
                                        .metric(Metric.builder()
                                                .namespace("AWS/RDS")
                                                .metricName("CPUUtilization")
                                                .dimensions(Dimension.builder()
                                                        .name("DBInstanceIdentifier")
                                                        .value(databaseIdentifier)
                                                        .build())
                                                .build())
                                        .period(60)
                                        .stat("Maximum")
                                        .build())
                                .returnData(true)
                                .build(),
                        MetricDataQuery.builder()
                                .id("memoryUsage")
                                .metricStat(MetricStat.builder()
                                        .metric(Metric.builder()
                                                .namespace("AWS/RDS")
                                                .metricName("FreeableMemory")
                                                .dimensions(Dimension.builder()
                                                        .name("DBInstanceIdentifier")
                                                        .value(databaseIdentifier)
                                                        .build())
                                                .build())
                                        .period(60)
                                        .stat("SampleCount")
                                        .build())
                                .returnData(true)
                                .build())
                .build();

        GetMetricDataResponse metricDataResponse = cloudWatchClient.getMetricData(metricDataRequest);
        List<MetricDataResult> metricDataResults = metricDataResponse.metricDataResults();
        Map<String, Double> peakValues = metricDataResults.stream()
                .collect(Collectors.toMap(
                        MetricDataResult::id,
                        result -> result.values().stream()
                                .max(Double::compareTo)
                                .orElse(-99999.9999)));

        return peakValues;
    }
}
