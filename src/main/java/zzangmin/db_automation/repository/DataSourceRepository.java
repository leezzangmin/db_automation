package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.MonitorTargetDb;

@Repository
public interface DataSourceRepository extends JpaRepository<MonitorTargetDb, Long> {

}
