package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.SlackDatabaseRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackDatabaseRequestRepository extends JpaRepository<SlackDatabaseRequest, Long> {

    Optional<SlackDatabaseRequest> findOneByRequestUuid(String requestUUID);

    @Query("select s from SlackDatabaseRequest s where s.executeStatus = 'IN_PROGRESS'")
    List<SlackDatabaseRequest> findInProgress();

    @Query("select s.executeStatus from SlackDatabaseRequest s where s.requestUuid =:requestUUID")
    Optional<SlackDatabaseRequest.ExecuteStatus> findExecuteStatusByRequestUuid(String requestUUID);
}
