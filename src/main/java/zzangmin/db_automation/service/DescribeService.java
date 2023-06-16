package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import zzangmin.db_automation.client.AwsClient;
import zzangmin.db_automation.dto.response.RdsClustersResponseDTO;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DescribeService {

    private final AwsClient awsClient;

    // TODO: 캐싱(mysql) / 상태저장 (?) 필요
    // db에 정보 넣어두는 테이블1, 업데이트 정보 관리하는 테이블2, HTTP 캐싱처럼 테이블2만 수시 조회 -> 업데이트 정보 있으면 getAndDel + awscli호출 + 테이블1업데이트
    public RdsClustersResponseDTO findClusters() {
        RdsClient rdsClient = awsClient.getRdsClient();
        DescribeDbInstancesResponse describeDbInstancesResponse = rdsClient.describeDBInstances();
        List<DBInstance> dbInstances = describeDbInstancesResponse.dbInstances();
        return RdsClustersResponseDTO.of(dbInstances);
    }

}
