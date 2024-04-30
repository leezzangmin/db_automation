package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.MysqlAccount;

import java.util.List;

@Repository
public interface MysqlAccountRepository extends JpaRepository<MysqlAccount, Long> {
    List<MysqlAccount> findAll();
}
