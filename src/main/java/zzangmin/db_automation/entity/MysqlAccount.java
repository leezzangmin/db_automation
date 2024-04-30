package zzangmin.db_automation.entity;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * column 단위 권한은 고려되지 않음
 * -> GRANT SELECT (`Company`), SHOW VIEW ON Reports.`Users` to 'chartio_read_only'@`localhost`;
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class MysqlAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String host;
    @Column(nullable = false)
    private String user;
    @OneToMany(mappedBy = "mysqlAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Privilege> privileges = new ArrayList<>();

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Entity
    static class Privilege {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne
        @JoinColumn(name = "mysql_account_id", nullable = false)
        private MysqlAccount mysqlAccount;
        @Column(nullable = false)
        private String databaseName;
        @Column(nullable = false)
        private String objectName;
        @Column(nullable = false)
        private String permissionType;
    }
}