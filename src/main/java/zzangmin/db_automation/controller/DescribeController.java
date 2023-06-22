package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.dto.response.RdsClustersResponseDTO;
import zzangmin.db_automation.service.DescribeService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DescribeController {

    private final DescribeService describeService;

    @GetMapping("/describe/cluster")
    public String describeRDSInstance(String instanceIdentifier) {
        return "ok";
    }

    @GetMapping("/describe/clusters")
    public RdsClustersResponseDTO describeRdsClusters() {
        return describeService.findClusters();
    }

    @GetMapping("/describe/cluster/status")
    public void describeRdsInstance() {

    }


//    @GetMapping("/ping")
//    public List<String> healthCheck() {
//        return Arrays.asList("hello", LocalDateTime.now().toString());
//    }

}
