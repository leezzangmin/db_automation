package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.SlackUser;

import java.util.Optional;

@Repository
public interface SlackUserRepository extends JpaRepository<SlackUser, Long> {
    Optional<SlackUser> findByUserSlackId(String userSlackId);
}
