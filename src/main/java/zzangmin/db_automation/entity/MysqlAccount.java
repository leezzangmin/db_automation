package zzangmin.db_automation.entity;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * EXECUTE FUNCTION, role, column 단위 권한은 고려되지 않음
 * -> GRANT EXECUTE ON FUNCTION `dbname`.`function_name` TO 'username'@'localhost'
 * -> GRANT rds_superuser_role@% TO admin@%
 * -> GRANT SELECT (`Company`), SHOW VIEW ON Reports.`Users` to 'chartio_read_only'@`localhost`;
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
@Entity
public class MysqlAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String serviceName;
    @Column(nullable = false)
    private String user;
    @Column(nullable = false)
    private String host;
    @ToString.Exclude
    @OneToMany(mappedBy = "mysqlAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Privilege> privileges = new ArrayList<>();

    public String getAccountString() {
        return "'" + this.user + "'@'" + this.host + "'";
    }
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Entity
    @Table(name = "mysql_account_privilege")
    public static class Privilege {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ToString.Exclude
        @ManyToOne
        @JoinColumn(name = "mysql_account_id", nullable = false)
        private MysqlAccount mysqlAccount;
        @Column(nullable = false)
        private String databaseName;
        @Column(nullable = false)
        private String objectName;
        @Column(nullable = false)
        private String permissionType;

        public static List<Privilege> dclToEntities(String grantDCL) {
            List<Privilege> privileges = new ArrayList<>();
            grantDCL = grantDCL.replace("`", "").replace(", ", ",");
            String[] parts = grantDCL.split(" ON ");
            String[] permissionParts = parts[0].replace("GRANT ", "").split(",");
            String databaseAndTable = parts[1].split(" TO ")[0].trim();

            for (String permission : permissionParts) {
                Privilege privilege = Privilege.builder()
                        .databaseName(databaseAndTable.split("\\.")[0])
                        .objectName(databaseAndTable.split("\\.")[1])
                        .permissionType(permission.trim())
                        .build();
                privileges.add(privilege);
            }
            if (grantDCL.contains("WITH GRANT OPTION")) {
                privileges.add(Privilege.builder()
                        .databaseName(databaseAndTable.split("\\.")[0])
                        .objectName(databaseAndTable.split("\\.")[1])
                        .permissionType("WITH GRANT OPTION")
                        .build());
            }
            return privileges;
        }
    }
}
