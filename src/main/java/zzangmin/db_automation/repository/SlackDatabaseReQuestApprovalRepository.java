package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.SlackDatabaseRequestApproval;

import java.util.List;

@Repository
public interface SlackDatabaseReQuestApprovalRepository extends JpaRepository<SlackDatabaseRequestApproval, Long> {

    @Query("SELECT ap from SlackDatabaseRequestApproval ap join fetch ap.slackDatabaseRequest s where s.requestUUID = :requestUUID ")
    List<SlackDatabaseRequestApproval> findByDatabaseRequestUUID(String requestUUID);
}
