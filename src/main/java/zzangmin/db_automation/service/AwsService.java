package zzangmin.db_automation.service;

import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;

import java.util.List;
import java.util.Map;

public interface AwsService {

    Map<String, Double> findRdsPeakCpuAndMemoryUsage(String databaseIdentifier);

    DescribeDbInstancesResponse findAllRdsInstanceInfo();

    Map<String, Long> findAllInstanceMetricsInfo(String databaseIdentifiers);

}
