package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zzangmin.db_automation.entity.MysqlAccount;

public interface PrivilegeRepository extends JpaRepository<MysqlAccount.Privilege, Long> {
}
