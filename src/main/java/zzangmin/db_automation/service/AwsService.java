package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.pi.PiClient;
import software.amazon.awssdk.services.pi.model.GetResourceMetricsRequest;
import software.amazon.awssdk.services.pi.model.GetResourceMetricsResponse;
import software.amazon.awssdk.services.pi.model.MetricQuery;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import software.amazon.awssdk.services.rds.model.Tag;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import zzangmin.db_automation.client.AwsClient;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.SecretManagerStandard;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.ParameterGroupStandard;
import zzangmin.db_automation.schedule.standardcheck.standardvalue.TagStandard;
import zzangmin.db_automation.util.ProfileUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static zzangmin.db_automation.schedule.standardcheck.standardvalue.SecretManagerStandard.DB_CREDENTIAL_POSTPIX;

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsService {

    private final AwsClient awsClient;
    private static final int DURATION_MINUTE = 5;
    private static final int PERIOD_SECONDS = 60 * DURATION_MINUTE;
    private static final String RDS_SERVICE_TYPE = "RDS";
    private static final int MIN_RECORD_SIZE = 20;



    public List<String> findDbParameterGroupNames() {
        List<String> dbParameterGroupNames = new ArrayList<>();

        List<DBInstance> dbInstances = findAllInstanceInfo();

        for (DBInstance dbInstance : dbInstances) {
            List<DBParameterGroupStatus> dbParameterGroupStatuses = dbInstance.dbParameterGroups();
            for (DBParameterGroupStatus dbParameterGroupStatus : dbParameterGroupStatuses) {
                String dbParameterGroupName = dbParameterGroupStatus.dbParameterGroupName();
                dbParameterGroupNames.add(dbParameterGroupName);
            }
        }
        return dbParameterGroupNames;
    }

    public List<String> findClusterParameterGroupNames() {
        List<String> clusterParameterGroupNames = new ArrayList<>();

        List<DBCluster> dbClusters = findAllClusterInfo();

        for (DBCluster dbCluster : dbClusters) {
            String clusterParameterGroupName = dbCluster.dbClusterParameterGroup();
            clusterParameterGroupNames.add(clusterParameterGroupName);
        }
        return clusterParameterGroupNames;
    }

    public String findClusterMasterUserName(String databaseIdentifier) {
        /**
         * writer 의 masterUsername return
         */
        log.info("findClusterMasterUserName databaseIdentifier: {}", databaseIdentifier);
        DescribeDbInstancesResponse instancesResponse = awsClient.getRdsClient()
                .describeDBInstances();
        for (DBInstance dbInstance : instancesResponse.dbInstances()) {
            log.info("dbInstance: {}", dbInstance);
            List<String> readReplicaDBInstanceIdentifiers = dbInstance.readReplicaDBInstanceIdentifiers();
            if (!readReplicaDBInstanceIdentifiers.contains(dbInstance.dbInstanceIdentifier())
                    && dbInstance.dbInstanceIdentifier().startsWith(databaseIdentifier)) {
                return dbInstance.masterUsername();
            }
        }
        throw new IllegalStateException("Writer masterUsername not found");
    }

    public List<Parameter> findClusterParameterGroupParameters(String parameterGroupName) {
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
                        .maxRecords(Math.max(MIN_RECORD_SIZE, ParameterGroupStandard.standardParameters.size()))
                        .build()
        );
        List<Parameter> dbParameters = describeDbClusterParametersResponse.parameters();
        log.info("cluster parameters: {}", dbParameters);
        return dbParameters;
    }

    public List<Parameter> findDbParameterGroupParameters(String parameterGroupName) {
        RdsClient rdsClient = awsClient.getRdsClient();

        DescribeDbParametersResponse describeDbParametersResponse = rdsClient.describeDBParameters(
                DescribeDbParametersRequest.builder()
                        .filters(ParameterGroupStandard.standardParameters.keySet()
                                .stream()
                                .map(parameterName -> Filter.builder()
                                        .name("parameter-name")
                                        .values(parameterName)
                                        .build())
                                .collect(Collectors.toList()))
                        .dbParameterGroupName(parameterGroupName)
                        .maxRecords(Math.max(MIN_RECORD_SIZE, ParameterGroupStandard.standardParameters.size()))
                        .build()
        );
        List<Parameter> dbParameters = describeDbParametersResponse.parameters();
        log.info("db parameters: {}", dbParameters);
        return dbParameters;
    }

    public String findRdsPassword(String serviceNameTagValue) {
        String secretName = SecretManagerStandard.generateStandardSecretManagerName(serviceNameTagValue, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE);
        log.info("secretName: {}", secretName);

        String password;
        GetSecretValueResponse valueResponse;

        SecretsManagerClient secretManagerClient = awsClient.getSecretManagerClient();
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        try {
            valueResponse = secretManagerClient.getSecretValue(valueRequest);
        } catch (Exception e) {
            throw new IllegalStateException(secretName + " 암호 정보가 secret manager에 존재하지 않습니다. convention: [ServiceName]-[PROFILE]" + DB_CREDENTIAL_POSTPIX);
        }

        try {
            password = new JSONObject(valueResponse.secretString())
                    .getString("password");
        } catch (Exception e) {
            throw new IllegalStateException("rds password fetch failed");
        }

        return password;
    }

    public String findRdsUsername(String serviceNameTagValue) {
        String secretName = SecretManagerStandard.generateStandardSecretManagerName(serviceNameTagValue, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE);
        log.info("secretName: {}", secretName);
        String username;
        GetSecretValueResponse valueResponse;

        SecretsManagerClient secretManagerClient = awsClient.getSecretManagerClient();
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        try {
            valueResponse = secretManagerClient.getSecretValue(valueRequest);
        } catch (Exception e) {
            throw new IllegalStateException(secretName + " 암호 정보가 secret manager에 존재하지 않습니다. convention: [ServiceName]-[PROFILE]" + DB_CREDENTIAL_POSTPIX);        }
        try {
            username = new JSONObject(valueResponse.secretString())
                    .getString("username");

        } catch (Exception e) {
            throw new IllegalStateException("rds username fetch failed");
        }

        return username;
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
        //RdsClient rdsClient = awsClient.getRdsClient();
        List<DBInstance> standaloneInstances = new ArrayList<>();
        List<RdsClient> rdsClients = awsClient.getRdsClients();
        for (RdsClient rdsClient : rdsClients) {
            DescribeDbInstancesResponse describeDbInstancesResponse = rdsClient.describeDBInstances();
            standaloneInstances.addAll(findValidInstances(describeDbInstancesResponse));
        }
//        DescribeDbInstancesResponse describeDbInstancesResponse = rdsClient.describeDBInstances();
//
//        List<DBInstance> standaloneInstances = findValidInstances(describeDbInstancesResponse);
        log.info("standaloneInstances: {}", standaloneInstances);
        return standaloneInstances;
    }

    // 클러스터에 속하지 않은 인스턴스만 필터링
    private List<DBInstance> findValidInstances(DescribeDbInstancesResponse describeDbInstancesResponse) {
        List<String> clusterInstanceIdentifiers = describeDbInstancesResponse.dbInstances()
                .stream()
                .filter(dbInstance -> dbInstance.dbClusterIdentifier() != null)
                .map(DBInstance::dbInstanceIdentifier)
                .collect(Collectors.toList());

        List<DBInstance> standaloneInstances = describeDbInstancesResponse.dbInstances().stream()
                .filter(dbInstance -> !clusterInstanceIdentifiers.contains(dbInstance.dbInstanceIdentifier()))
                .filter(dbInstance -> dbInstance.dbInstanceStatus().equals("available"))
                .filter(dbInstance -> !dbInstance.tagList().contains(TagStandard.standardTagKeyNames))
                .filter(dbInstance -> isCurrentEnvHasValidTag(dbInstance.tagList()))
                .collect(Collectors.toList());
        return standaloneInstances;
    }

    public List<DBCluster> findAllClusterInfo() {
        List<DBCluster> dbClusters = new ArrayList<>();

        List<RdsClient> rdsClients = awsClient.getRdsClients();
        for (RdsClient rdsClient : rdsClients) {
            DescribeDbClustersResponse describeDbClustersResponse = rdsClient.describeDBClusters();
            DescribeDbClustersResponse clustersResponse = findValidClusters(describeDbClustersResponse);
            dbClusters.addAll(clustersResponse.dbClusters());
        }
//        DescribeDbClustersResponse describeDbClustersResponse = awsClient.getRdsClient()
//                .describeDBClusters();
//        DescribeDbClustersResponse clustersResponse = findValidClusters(describeDbClustersResponse);
//
//        List<DBCluster> dbClusters = clustersResponse.dbClusters();
        log.info("clusters: {}", dbClusters);
        return dbClusters;
    }

    private DescribeDbClustersResponse findValidClusters(DescribeDbClustersResponse describeDbClustersResponse) {
        DescribeDbClustersResponse clustersResponse = DescribeDbClustersResponse.builder()
                .dbClusters(describeDbClustersResponse.dbClusters().stream()
                        .filter(cluster -> cluster.status().equals("available"))
                        .filter(cluster -> !cluster.tagList().contains(TagStandard.standardTagKeyNames))
                        .filter(cluster -> isCurrentEnvHasValidTag(cluster.tagList()))
                        .collect(Collectors.toList()))
                .build();
        return clustersResponse;
    }

    public Map<String, Long> findAllInstanceMetricsInfo(String databaseIdentifier) {
        CloudWatchClient cloudWatchClient = awsClient.getCloudWatchClient();
        PiClient performanceInsightClient = awsClient.getPerformanceInsightClient();
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMinutes(DURATION_MINUTE));

        // TODO: 프리티어에서는 performance insights 사용 불가능, (DbiResourceId 로 변경)
        GetResourceMetricsRequest piRequest = generateAverageActiveSessionsRequest(findWriterInstanceDbiResourceId(databaseIdentifier));
        GetResourceMetricsResponse getResourceMetricsResponse = performanceInsightClient.getResourceMetrics(piRequest);
        Double averageActiveSession = getResourceMetricsResponse.metricList().get(0).dataPoints().get(0).value();


        GetMetricDataRequest cpuMemoryConnectionDiskRequest = GetMetricDataRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .scanBy(ScanBy.TIMESTAMP_ASCENDING)
                .metricDataQueries(
                        generateCpuUsageMetricQuery(databaseIdentifier),
                        generateMemoryUsageMetricQuery(databaseIdentifier),
                        generateReadQpsMetricQuery(databaseIdentifier),
                        generateWriteQpsMetricQuery(databaseIdentifier),
                        generateConnectionMetricQuery(databaseIdentifier),
                        generateDiskUsageMetricQuery(databaseIdentifier))
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
        log.info("databaseIdentifier: {}", databaseIdentifier);
        DescribeDbInstancesResponse instancesResponse = awsClient.getRdsClient()
                .describeDBInstances();
        for (DBInstance dbInstance : instancesResponse.dbInstances()) {
            log.info("dbInstance: {}", dbInstance);
            List<String> readReplicaDBInstanceIdentifiers = dbInstance.readReplicaDBInstanceIdentifiers();
            if (!readReplicaDBInstanceIdentifiers.contains(dbInstance.dbInstanceIdentifier())
                    && dbInstance.dbInstanceIdentifier().startsWith(databaseIdentifier)) {
                return dbInstance.dbiResourceId();
            }
        }
        throw new IllegalStateException("Writer DbiResourceId not found");
    }

    // 환경변수 프로파일이 prod인데 tag의 값이 stage면 false 반환
    private boolean isCurrentEnvHasValidTag(List<Tag> tags) {
        log.info("tags: {}, profile: {}", tags, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE);

        for (Tag tag : tags) {
            if (tag.key().equals(TagStandard.getEnvironmentTagKeyName())) {
                if (tag.value().equals(ProfileUtil.CURRENT_ENVIRONMENT_PROFILE)) {
                    return true;
                }
            }
        }
        return false;
    }

}
