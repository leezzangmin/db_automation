package zzangmin.db_automation.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RDS 클러스터 상태 정보 목록 DTO
 * 이름, 클러스터 타입, uptime, (설정 ? ), cpu(총용량,사용량), memory, AAS, connection,
 * dbInstance = DBInstance(DBInstanceIdentifier=zzangmin-db, DBInstanceClass=db.t3.micro, Engine=mysql, DBInstanceStatus=available, MasterUsername=admin, Endpoint=Endpoint(Address=zzangmin-db.codf49uhek24.ap-northeast-2.rds.amazonaws.com, Port=3306, HostedZoneId=ZLA2NUCOLGUUR),
 * AllocatedStorage=20, InstanceCreateTime=2023-06-05T05:34:39.869Z, PreferredBackupWindow=19:57-20:27, BackupRetentionPeriod=7, DBSecurityGroups=[],
 * VpcSecurityGroups=[VpcSecurityGroupMembership(VpcSecurityGroupId=sg-0cec7b7fdb57355c8, Status=active)],
 * DBParameterGroups=[DBParameterGroupStatus(DBParameterGroupName=default.mysql8.0, ParameterApplyStatus=in-sync)],
 * AvailabilityZone=ap-northeast-2c, DBSubnetGroup=DBSubnetGroup(DBSubnetGroupName=default-vpc-0f5fcf1767ca93dd5,
 * DBSubnetGroupDescription=Created from the RDS Management Console, VpcId=vpc-0f5fcf1767ca93dd5, SubnetGroupStatus=Complete,
 * Subnets=[Subnet(SubnetIdentifier=subnet-01bb8ff2e42f87c4d, SubnetAvailabilityZone=AvailabilityZone(Name=ap-northeast-2d),
 * SubnetOutpost=Outpost(), SubnetStatus=Active), Subnet(SubnetIdentifier=subnet-0a716d522be9a519c,
 * SubnetAvailabilityZone=AvailabilityZone(Name=ap-northeast-2b), SubnetOutpost=Outpost(), SubnetStatus=Active),
 * Subnet(SubnetIdentifier=subnet-0cefb2b7030cf4a4d, SubnetAvailabilityZone=AvailabilityZone(Name=ap-northeast-2a),
 * SubnetOutpost=Outpost(), SubnetStatus=Active), Subnet(SubnetIdentifier=subnet-01410b56fe2cc772b,
 * SubnetAvailabilityZone=AvailabilityZone(Name=ap-northeast-2c), SubnetOutpost=Outpost(), SubnetStatus=Active)]),
 * PreferredMaintenanceWindow=fri:17:02-fri:17:32, PendingModifiedValues=PendingModifiedValues(),
 * LatestRestorableTime=2023-06-16T02:35:00Z, MultiAZ=false, EngineVersion=8.0.32, AutoMinorVersionUpgrade=false,
 * ReadReplicaDBInstanceIdentifiers=[], LicenseModel=general-public-license,
 * OptionGroupMemberships=[OptionGroupMembership(OptionGroupName=default:mysql-8-0, Status=in-sync)],
 * PubliclyAccessible=true, StorageType=gp2, DbInstancePort=0, StorageEncrypted=false,
 * DbiResourceId=db-G4JGZOH73B5UYRCBQ24CLGLIGA, CACertificateIdentifier=rds-ca-2019, DomainMemberships=[],
 * CopyTagsToSnapshot=false, MonitoringInterval=60,
 * EnhancedMonitoringResourceArn=arn:aws:logs:ap-northeast-2:431565954522:log-group:RDSOSMetrics:log-stream:db-G4JGZOH73B5UYRCBQ24CLGLIGA,
 * MonitoringRoleArn=arn:aws:iam::431565954522:role/rds-monitoring-role,
 * DBInstanceArn=arn:aws:rds:ap-northeast-2:431565954522:db:zzangmin-db,
 * IAMDatabaseAuthenticationEnabled=false, PerformanceInsightsEnabled=false, DeletionProtection=true,
 * AssociatedRoles=[], MaxAllocatedStorage=1000, TagList=[], CustomerOwnedIpEnabled=false)
 */


@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RdsClustersResponseDTO {

    private List<RdsClusterResponseDTO> clusters = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    public static class RdsClusterResponseDTO {
        private String clusterName;
        private String engineVersion;
        private boolean multiAzEnabled;
        private boolean autoMinorVersionUpgradeEnabled;
        private boolean performanceInsightsEnabled;
        private boolean deletionProtectionEnabled;
        private String instanceClass; // 인스턴스 타입


        //private LocalDateTime uptime;

        private int cpuUsage;
        private long freeableMemory;
        private int averageActiveSession;
        private int connection;
        private long freeStorageSpace;
        private long selectThroughput;
        private long writeThroughput;


        public static RdsClusterResponseDTO of(DBInstance dbInstance, Map<String, Long> metrics) {
            String clusterName = dbInstance.getValueForField("DBInstanceIdentifier", String.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));
            String engineVersion = dbInstance.getValueForField("EngineVersion", String.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));
            boolean multiAzEnabled = dbInstance.getValueForField("MultiAZ", Boolean.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));
            boolean autoMinorVersionUpgradeEnabled = dbInstance.getValueForField("AutoMinorVersionUpgrade", Boolean.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));
            boolean performanceInsightsEnabled = dbInstance.getValueForField("PerformanceInsightsEnabled", Boolean.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));
            boolean deletionProtectionEnabled = dbInstance.getValueForField("DeletionProtection", Boolean.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));
            String instanceClass = dbInstance.getValueForField("DBInstanceClass", String.class)
                    .orElseThrow(() -> new IllegalArgumentException("DBInstanceDTO 파싱 오류"));

            // ======================================================================================

            int cpuUsage = metrics.get("cpuUsage").intValue();
            long memoryUsage = metrics.get("freeableMemory");
            int averageActiveSession = metrics.get("averageActiveSession").intValue();
            int connection = metrics.get("connection").intValue();
            long diskUsage = metrics.get("freeStorageSpace");
            long selectThroughput = metrics.get("readThroughput");
            long writeThroughput = metrics.get("writeThroughput");

            return new RdsClusterResponseDTO(clusterName, engineVersion, multiAzEnabled, autoMinorVersionUpgradeEnabled, performanceInsightsEnabled, deletionProtectionEnabled,
                    instanceClass, cpuUsage, memoryUsage, averageActiveSession, connection, diskUsage, selectThroughput, writeThroughput);
        }
    }

    public static RdsClustersResponseDTO of(List<DBInstance> dbInstances, List<Map<String, Long>> metricsList) {
        List<RdsClusterResponseDTO> clusters = new ArrayList<>();
        for (int i = 0; i < dbInstances.size(); i++) {
            clusters.add(RdsClusterResponseDTO.of(dbInstances.get(i), metricsList.get(i)));
        }
        return new RdsClustersResponseDTO(clusters);
    }

}
