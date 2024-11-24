package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.MonitorTargetDatabase;

@Repository
public interface MonitorTargetDatabaseRepository extends JpaRepository<MonitorTargetDatabase, Long> {
}
