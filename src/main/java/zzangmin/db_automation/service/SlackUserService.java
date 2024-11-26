package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.entity.SlackUser;
import zzangmin.db_automation.repository.SlackUserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackUserService {

    private final SlackUserRepository slackUserRepository;

    /**
     * 특정 유저(admin)만 request 를 승인/반려 할 수 있음.
     *
     */
    @Transactional(readOnly = true)
    public void validateRequestAcceptDoerAdmin(String slackUserId) {
        SlackUser slackUser = slackUserRepository.findByUserSlackId(slackUserId)
                .orElseThrow(() -> new IllegalStateException(slackUserId + " : 해당 id의 Slack User 가 DB에 존재하지 않습니다."));

        if (!slackUser.getUserType().equals(SlackUser.SlackUserType.ADMIN)) {
            throw new IllegalArgumentException(slackUserId + ": 해당 user 가 처리할 수 없는 action 입니다.");
        }
    }

    @Transactional(readOnly = true)
    public SlackUser findSlackUser(String slackUserId) {
        SlackUser slackUser = slackUserRepository.findByUserSlackId(slackUserId)
                .orElseThrow(() -> new IllegalStateException(slackUserId + " : 해당 slack user ID 유저가 테이블에 존재하지 않습니다."));
        return slackUser;
    }
}
