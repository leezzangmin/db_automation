package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.repository.DataSourceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataSourceService {

    private final DataSourceRepository dataSourceRepository;

    public List<DatabaseConnectionInfo> findMonitorTargetDbs() {
        return dataSourceRepository.findAll()
                .stream()
                .filter(d -> !d.isMonitorTarget())
                .map(DatabaseConnectionInfo::of)
                .collect(Collectors.toList());
    }
}
