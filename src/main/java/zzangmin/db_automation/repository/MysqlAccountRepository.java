package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.MysqlAccount;

import java.util.List;
import java.util.Optional;

@Repository
public interface MysqlAccountRepository extends JpaRepository<MysqlAccount, Long> {

    @Query
    Optional<MysqlAccount> findByServiceNameAndUserAndHost(String serviceName, String user, String host);

    @Query
    List<MysqlAccount> findByServiceName(String serviceName);
}
