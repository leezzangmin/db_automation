package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.response.RdsClusterSchemaTablesResponseDTO;
import zzangmin.db_automation.dto.response.RdsClusterSchemaTablesResponseDTO.TableInfo;
import zzangmin.db_automation.dto.response.RdsClustersResponseDTO;
import zzangmin.db_automation.entity.TableStatus;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class DescribeService {

    public static final List<String> schemaBlackList = List.of("information_schema", "mysql", "performance_schema", "sys");

    private final AwsService awsService;
    private final MysqlClient mysqlClient;

    // TODO: 캐싱(mysql) / 상태저장 (?) 필요
    // db에 정보 넣어두는 테이블1, 업데이트 정보 관리하는 테이블2, HTTP 캐싱처럼 테이블2만 수시 조회
    // -> 업데이트 정보 있으면 getAndDel + awscli호출 + 테이블1업데이트
    public RdsClustersResponseDTO findClustersInfo() {
        DescribeDbInstancesResponse allRdsInstanceInfo = awsService.findAllRdsInstanceInfo();
        List<DBInstance> dbInstancesInfo = allRdsInstanceInfo.dbInstances();
        List<Map<String, Long>> rdsInfo = new ArrayList<>();
        dbInstancesInfo.stream()
                .map(i -> i.getValueForField("DBInstanceIdentifier", String.class).get())
                .forEach(i -> rdsInfo.add(awsService.findAllInstanceMetricsInfo(i)));
        return RdsClustersResponseDTO.of(dbInstancesInfo, rdsInfo);
    }

    // 클러스터의 서비스용 스키마의 테이블 목록(용량, 등)
    // TODO: 페이징, 정렬 (용량,이름,행수), 검색
    public List<RdsClusterSchemaTablesResponseDTO> findClusterTables(DatabaseConnectionInfo databaseConnectionInfo) {
        List<RdsClusterSchemaTablesResponseDTO> rdsClusterSchemaTablesResponseDTOs = new ArrayList<>();
        Set<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                .stream()
                .filter(s -> !schemaBlackList.contains(s))
                .collect(Collectors.toSet());
        for (String schemaName : schemaNames) {
            Set<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
            List<TableInfo> tableInfos = mysqlClient.findTableStatuses(databaseConnectionInfo, schemaName, tableNames)
                    .stream()
                    .map(tableStatus -> new TableInfo(tableStatus.getTableName(), tableStatus.calculateTotalTableByteSize(), tableStatus.getTableRow()))
                    .collect(Collectors.toList());
            rdsClusterSchemaTablesResponseDTOs.add(new RdsClusterSchemaTablesResponseDTO(schemaName, tableInfos));
        }
        return rdsClusterSchemaTablesResponseDTOs;
    }

    public void findTableInfo(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {

    }
}
