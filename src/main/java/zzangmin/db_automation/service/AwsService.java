package zzangmin.db_automation.service;

import java.util.Map;

public interface AwsService {

    Map<String, Double> findRdsPeakCpuAndMemoryUsage(String databaseIdentifier);
}
