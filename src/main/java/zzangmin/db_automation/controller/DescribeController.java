package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.response.RdsClusterSchemaTablesResponseDTO;
import zzangmin.db_automation.dto.response.RdsClustersResponseDTO;
import zzangmin.db_automation.dto.response.TableInfoResponseDTO;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DescribeService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DescribeController {

    private final DescribeService describeService;

    @GetMapping("/describe/cluster")
    public String describeRDSInstance(String instanceIdentifier) {
        return "ok";
    }

    @GetMapping("/describe/clusterssssssss")
    public RdsClustersResponseDTO describeRdsCluster() {
        return describeService.findClustersInfo();
    }

    @GetMapping("/describe/cluster/status")
    public List<RdsClusterSchemaTablesResponseDTO> describeRdsInstance(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo) {
        return describeService.findClusterTables(databaseConnectionInfo);
    }


    @GetMapping("/describe/table")
    public TableInfoResponseDTO describeTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                              @RequestParam String schemaName, @RequestParam String tableName) {
        return describeService.findTableInfo(databaseConnectionInfo, schemaName, tableName);
    }

}
