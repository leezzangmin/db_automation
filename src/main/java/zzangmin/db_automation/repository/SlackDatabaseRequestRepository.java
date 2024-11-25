package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.SlackDatabaseRequest;

import java.util.Optional;

@Repository
public interface SlackDatabaseRequestRepository extends JpaRepository<SlackDatabaseRequest, Long> {

    Optional<SlackDatabaseRequest> findOneByRequestUUID(String requestUUID);
}
