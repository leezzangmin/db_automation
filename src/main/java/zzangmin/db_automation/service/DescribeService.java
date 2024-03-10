package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DescribeDbClustersResponse;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.dto.response.*;
import zzangmin.db_automation.dto.response.RdsClusterSchemaTablesResponseDTO.TableInfo;
import zzangmin.db_automation.entity.ChangeHistory;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.repository.ChangeHistoryRepository;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class DescribeService {

    public static final List<String> schemaBlackList = List.of("information_schema", "mysql", "performance_schema", "sys", "dba");

    private final AwsService awsService;
    private final MysqlClient mysqlClient;
    private final ChangeHistoryRepository changeHistoryRepository;
    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    public RdsClustersResponseDTO findClustersInfo() {
        DescribeDbClustersResponse allClusterInfo = awsService.findAllClusterInfo();
        List<DBCluster> dbClusters = allClusterInfo.dbClusters();
        List<Map<String, Long>> rdsInfo = new ArrayList<>();
        dbClusters.stream()
                .map(i -> i.getValueForField("DBClusterIdentifier", String.class).get())
                .forEach(i -> rdsInfo.add(awsService.findAllInstanceMetricsInfo(i)));
        return RdsClustersResponseDTO.of(dbClusters, rdsInfo);
    }

    // 클러스터의 서비스용 스키마의 테이블 목록(용량, 등)
    public List<RdsClusterSchemaTablesResponseDTO> findClusterTables(DatabaseConnectionInfo databaseConnectionInfo) {
        List<RdsClusterSchemaTablesResponseDTO> rdsClusterSchemaTablesResponseDTOs = new ArrayList<>();
        List<String> schemaNames = mysqlClient.findSchemaNames(databaseConnectionInfo)
                .stream()
                .filter(s -> !schemaBlackList.contains(s))
                .collect(Collectors.toList());
        for (String schemaName : schemaNames) {
            List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
            List<TableInfo> tableInfos = mysqlClient.findTableStatuses(databaseConnectionInfo, schemaName, tableNames)
                    .stream()
                    .map(tableStatus -> new TableInfo(tableStatus.getTableName(), tableStatus.calculateTotalTableByteSize(), tableStatus.getTableRow()))
                    .collect(Collectors.toList());
            rdsClusterSchemaTablesResponseDTOs.add(new RdsClusterSchemaTablesResponseDTO(schemaName, tableInfos));
        }
        return rdsClusterSchemaTablesResponseDTOs;
    }

    public DBMSNamesResponseDTO findDBMSNames() {
        List<String> dbmsNames = dynamicDataSourceProperties.getDatabases()
                .keySet()
                .stream()
                .collect(Collectors.toList());
        return new DBMSNamesResponseDTO(dbmsNames);
    }

    public TableInfoResponseDTO findTableInfo(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        List<ChangeHistory> changeHistories = changeHistoryRepository.findByDatabaseIdentifierAndSchemaNameAndTableName(databaseConnectionInfo.getDatabaseName(), schemaName, tableName);
        List<Column> columns = mysqlClient.findColumns(databaseConnectionInfo, schemaName, tableName);
        return new TableInfoResponseDTO(databaseConnectionInfo.getDatabaseName(), schemaName, tableName, columns, changeHistories);
    }

    public String findTableSchema(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        return mysqlClient.findCreateTableStatement(databaseConnectionInfo, schemaName, tableName);
    }

    public SchemaNamesResponseDTO findSchemaNames(DatabaseConnectionInfo databaseConnectionInfo) {
        return new SchemaNamesResponseDTO(databaseConnectionInfo.getDatabaseName(), mysqlClient.findSchemaNames(databaseConnectionInfo)
                .stream()
                .filter(s -> !schemaBlackList.contains(s))
                .collect(Collectors.toList()));
    }

    public TableNamesResponseDTO findTableNames(DatabaseConnectionInfo databaseConnectionInfo, String schemaName) {
        List<String> tableNames = mysqlClient.findTableNames(databaseConnectionInfo, schemaName);
        return new TableNamesResponseDTO(databaseConnectionInfo.getDatabaseName(), schemaName, tableNames);
    }
}
