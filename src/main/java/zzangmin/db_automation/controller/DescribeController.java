package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.response.*;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DescribeController {

    private final DescribeService describeService;

    @GetMapping("/describe/clusterNames")
    public ClusterNamesResponseDTO describeClusterNames() {
        return describeService.findClusterNames();
    }

    @GetMapping("/describe/cluster/schemaNames")
    public SchemaNamesResponseDTO describeSchemaNames(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo) {
        return describeService.findSchemaNames(databaseConnectionInfo);
    }

    @GetMapping("/describe/cluster/tableNames")
    public TableNamesResponseDTO describeTableNames(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                    @RequestParam String schemaName) {
        return describeService.findTableNames(databaseConnectionInfo, schemaName);
    }

    @GetMapping("/describe/clusters")
    public RdsClustersResponseDTO describeRdsCluster() {
        return describeService.findClustersInfo();
    }

    // 스키마 목록, 테이블 목록 및 사이즈
    @GetMapping("/describe/cluster/schemas")
    public List<RdsClusterSchemaTablesResponseDTO> describeRdsInstance(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo) {
        return describeService.findClusterTables(databaseConnectionInfo);
    }

    @GetMapping("/describe/table/status")
    public TableInfoResponseDTO describeTableStatus(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestParam String schemaName, @RequestParam String tableName) {
        return describeService.findTableInfo(databaseConnectionInfo, schemaName, tableName);
    }

    @GetMapping("/describe/table")
    public String describeTableSchema(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                                    @RequestParam String schemaName, @RequestParam String tableName) {
        return describeService.findTableSchema(databaseConnectionInfo, schemaName, tableName);
    }

}
