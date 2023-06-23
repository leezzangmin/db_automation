package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
//@Profile(value = "develop")
@Service
public class AwsServiceDevelopment implements AwsService {

    @Override
    public Map<String, Double> findRdsPeakCpuAndMemoryUsage(String databaseIdentifier) {
        return Map.of("cpuUsage", 50.0, "memoryUsage", 30.0);
    }

}