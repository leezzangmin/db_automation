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
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import zzangmin.db_automation.client.AwsClient;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.ParameterGroupStandard;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    public List<String> findParameterGroupNames() {
        List<String> clusterParameterGroupNames = new ArrayList<>();

        RdsClient rdsClient = awsClient.getRdsClient();
        DescribeDbClustersResponse describeDbClustersResponse = rdsClient.describeDBClusters();
        List<DBCluster> dbClusters = describeDbClustersResponse.dbClusters();
        for (DBCluster dbCluster : dbClusters) {
            String clusterParameterGroupName = dbCluster.dbClusterParameterGroup();
            clusterParameterGroupNames.add(clusterParameterGroupName);
        }
        return clusterParameterGroupNames;
    }

    public DescribeDbClusterParametersResponse findClusterParameterGroup(String parameterGroupName) {
        RdsClient rdsClient = awsClient.getRdsClient();
        DescribeDbClusterParametersResponse describeDbClusterParametersResponse = rdsClient.describeDBClusterParameters(
                DescribeDbClusterParametersRequest.builder()
                        .filters(ParameterGroupStandard.standardParameters.keySet()
                                .stream()
                                .map(parameterName -> Filter.builder()
                                        .name("parameter-name")
                                        .values(parameterName)
                                        .build())
                                .collect(Collectors.toList()))
                        .dbClusterParameterGroupName(parameterGroupName)
                        .build()
        );
        return describeDbClusterParametersResponse;
    }

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

    public List<DBInstance> findAllInstanceInfo() {
        RdsClient rdsClient = awsClient.getRdsClient();

        // 클러스터에 속하지 않은 인스턴스만 필터링
        DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
        List<DBInstance> allInstances = response.dbInstances();
        List<String> clusterInstanceIdentifiers = response.dbInstances()
                .stream()
                .filter(dbInstance -> dbInstance.dbClusterIdentifier() != null)
                .map(DBInstance::dbInstanceIdentifier)
                .collect(Collectors.toList());

        List<DBInstance> standaloneInstances = allInstances.stream()
                .filter(dbInstance -> !clusterInstanceIdentifiers.contains(dbInstance.dbInstanceIdentifier()))
                .collect(Collectors.toList());

        return standaloneInstances;
    }

    public DescribeDbClustersResponse findAllClusterInfo() {
        DescribeDbClustersResponse describeDbClustersResponse = awsClient.getRdsClient()
                .describeDBClusters();

        DescribeDbClustersResponse availableClustersResponse = DescribeDbClustersResponse.builder()
                .dbClusters(describeDbClustersResponse.dbClusters().stream()
                        .filter(cluster -> cluster.status().equals("available"))
                        .collect(Collectors.toList()))
                .build();

        return availableClustersResponse;
    }

    public List<String> findAllClusterNames() {
        RdsClient rdsClient = awsClient.getRdsClient();
        DescribeDbClustersResponse describeDbClustersResponse = rdsClient.describeDBClusters();
        return describeDbClustersResponse.dbClusters()
                .stream()
                .filter(cluster -> cluster.status().equals("available"))
                .map(cluster -> cluster.dbClusterIdentifier())
                .collect(Collectors.toList());

    }

    public Map<String, Long> findAllInstanceMetricsInfo(String databaseIdentifiers) {
        CloudWatchClient cloudWatchClient = awsClient.getCloudWatchClient();
        PiClient performanceInsightClient = awsClient.getPerformanceInsightClient();
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMinutes(DURATION_MINUTE));

        // TODO: 프리티어에서는 performance insights 사용 불가능, (DbiResourceId 로 변경)
         GetResourceMetricsRequest piRequest = generateAverageActiveSessionsRequest(findWriterInstanceDbiResourceId(databaseIdentifiers));
         GetResourceMetricsResponse getResourceMetricsResponse = performanceInsightClient.getResourceMetrics(piRequest);
        Double averageActiveSession = getResourceMetricsResponse.metricList().get(0).dataPoints().get(0).value();


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

        metrics.put("averageActiveSession", averageActiveSession.longValue());
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
                                        .name("DBClusterIdentifier")
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
                                        .name("DBClusterIdentifier")
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
                                        .name("DBClusterIdentifier")
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
                .id("freeLocalStorage")
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace("AWS/RDS")
                                .metricName("FreeLocalStorage")
                                .dimensions(Dimension.builder()
                                        .name("DBClusterIdentifier")
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
                                        .name("DBClusterIdentifier")
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
                                        .name("DBClusterIdentifier")
                                        .value(databaseIdentifier)
                                        .build())
                                .build())
                        .period(PERIOD_SECONDS)
                        .stat("Maximum")
                        .build())
                .returnData(true)
                .build();
    }

    private String findWriterInstanceDbiResourceId(String databaseIdentifier) {
        DescribeDbInstancesResponse instancesResponse = awsClient.getRdsClient().describeDBInstances();
        for (DBInstance dbInstance : instancesResponse.dbInstances()) {
            List<String> readReplicaDBInstanceIdentifiers = dbInstance.readReplicaDBInstanceIdentifiers();
            if (!readReplicaDBInstanceIdentifiers.contains(dbInstance.dbInstanceIdentifier()) && dbInstance.dbClusterIdentifier().equals(databaseIdentifier)) {
                return dbInstance.dbiResourceId();
            }
        }
        throw new IllegalStateException("Writer DbiResourceId not found");
    }

}
