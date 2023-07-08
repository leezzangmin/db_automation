package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.pi.PiClient;
import software.amazon.awssdk.services.pi.model.GetResourceMetricsRequest;
import software.amazon.awssdk.services.pi.model.GetResourceMetricsResponse;
import software.amazon.awssdk.services.pi.model.MetricQuery;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import zzangmin.db_automation.client.AwsClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsService {

    private final AwsClient awsClient;
    private static final int DURATION_MINUTE = 5;
    private static final int PERIOD_SECONDS = 60 * DURATION_MINUTE;
    private static final String RDS_SERVICE_TYPE = "RDS";

    public String findRdsPassword(String databaseIdentifier) {
        SsmClient ssmClient = awsClient.getSsmClient();
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(databaseIdentifier + "-password")
                .withDecryption(true)
                .build();
        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        return parameterResponse.parameter().value();
    }

    public Map<String, Double> findRdsPeakCpuAndMemoryUsage(String databaseIdentifier) {
        CloudWatchClient cloudWatchClient = awsClient.getCloudWatchClient();
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMinutes(DURATION_MINUTE));

        GetMetricDataRequest metricDataRequest = GetMetricDataRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .scanBy(ScanBy.TIMESTAMP_ASCENDING)
                .metricDataQueries(generateCpuUsageMetricQuery(databaseIdentifier),
                        generateMemoryUsageMetricQuery(databaseIdentifier))
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

    public DescribeDbInstancesResponse findAllRdsInstanceInfo() {
        return awsClient.getRdsClient()
                .describeDBInstances();
    }

    public Map<String, Long> findAllInstanceMetricsInfo(String databaseIdentifiers) {
        CloudWatchClient cloudWatchClient = awsClient.getCloudWatchClient();
        PiClient performanceInsightClient = awsClient.getPerformanceInsightClient();
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMinutes(DURATION_MINUTE));

        // TODO: 프리티어에서는 performance insights 사용 불가능, (DbiResourceId 로 변경)
        // GetResourceMetricsRequest piRequest = generateAverageActiveSessionsRequest("DbiResourceId");
        // GetResourceMetricsResponse averageActiveSessions = performanceInsightClient.getResourceMetrics(piRequest);
        GetResourceMetricsResponse averageActiveSessions = null;

        GetMetricDataRequest cpuMemoryConnectionDiskRequest = GetMetricDataRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .scanBy(ScanBy.TIMESTAMP_ASCENDING)
                .metricDataQueries(
                        generateCpuUsageMetricQuery(databaseIdentifiers),
                        generateMemoryUsageMetricQuery(databaseIdentifiers),
                        generateReadQpsMetricQuery(databaseIdentifiers),
                        generateWriteQpsMetricQuery(databaseIdentifiers),
                        generateConnectionMetricQuery(databaseIdentifiers),
                        generateDiskUsageMetricQuery(databaseIdentifiers))
                .build();

        GetMetricDataResponse metricData = cloudWatchClient.getMetricData(cpuMemoryConnectionDiskRequest);

        List<MetricDataResult> metricDataResults = metricData.metricDataResults();
        Map<String, Long> metrics = new HashMap<>();
        for (MetricDataResult metricDataResult : metricDataResults) {
            String metricLabel = metricDataResult.getValueForField("Id", String.class)
                    .orElseThrow(() -> new IllegalStateException("Metric id not found"));
            List<Double> singleValue = metricDataResult.getValueForField("Values", List.class)
                    .orElseThrow(() -> new IllegalStateException("Metric values not found"));
            Double metricValue = singleValue.get(0);
            metrics.put(metricLabel, metricValue.longValue());
        }
        metrics.put("averageActiveSession", 1234567L);
        return metrics;
    }

    private GetResourceMetricsRequest generateAverageActiveSessionsRequest(String dbiResourceId) {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMinutes(DURATION_MINUTE));
        return GetResourceMetricsRequest.builder()
                .identifier(dbiResourceId)
                .serviceType(RDS_SERVICE_TYPE)
                .metricQueries(MetricQuery.builder()
                        .metric("db.Users.Threads_running.avg")
                        .build())
                .startTime(startTime)
                .endTime(endTime)
                .periodInSeconds(PERIOD_SECONDS)
                .build();
    }

    private MetricDataQuery generateCpuUsageMetricQuery(String databaseIdentifier) {
        return MetricDataQuery.builder()
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
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

    private MetricDataQuery generateMemoryUsageMetricQuery(String databaseIdentifier) {
        return MetricDataQuery.builder()
                .id("freeableMemory")
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace("AWS/RDS")
                                .metricName("FreeableMemory")
                                .dimensions(Dimension.builder()
                                        .name("DBInstanceIdentifier")
                                        .value(databaseIdentifier)
                                        .build())
                                .build())
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

    private MetricDataQuery generateConnectionMetricQuery(String databaseIdentifier) {
        return MetricDataQuery.builder()
                .id("connection")
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace("AWS/RDS")
                                .metricName("DatabaseConnections")
                                .dimensions(Dimension.builder()
                                        .name("DBInstanceIdentifier")
                                        .value(databaseIdentifier)
                                        .build())
                                .build())
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

    private MetricDataQuery generateDiskUsageMetricQuery(String databaseIdentifier) {
        return MetricDataQuery.builder()
                .id("freeStorageSpace")
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace("AWS/RDS")
                                .metricName("FreeStorageSpace")
                                .dimensions(Dimension.builder()
                                        .name("DBInstanceIdentifier")
                                        .value(databaseIdentifier)
                                        .build())
                                .build())
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

    private MetricDataQuery generateReadQpsMetricQuery(String databaseIdentifier) {
        return MetricDataQuery.builder()
                .id("readThroughput")
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace("AWS/RDS")
                                .metricName("ReadThroughput")
                                .dimensions(Dimension.builder()
                                        .name("DBInstanceIdentifier")
                                        .value(databaseIdentifier)
                                        .build())
                                .build())
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

    private MetricDataQuery generateWriteQpsMetricQuery(String databaseIdentifier) {
        return MetricDataQuery.builder()
                .id("writeThroughput")
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace("AWS/RDS")
                                .metricName("WriteThroughput")
                                .dimensions(Dimension.builder()
                                        .name("DBInstanceIdentifier")
                                        .value(databaseIdentifier)
                                        .build())
                                .build())
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

}
