package zzangmin.db_automation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SlackDatabaseRequestApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slack_database_request_id", nullable = false)
    private SlackDatabaseRequest slackDatabaseRequest;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slack_user_id", nullable = false)
    private SlackUser slackUser;
    @Enumerated(EnumType.STRING)
    private ResponseType responseType;
    private String responseReason;
    private LocalDateTime responseDateTime;


    public enum ResponseType {
        ACCEPT,
        DENY,
        HOLD,
    }
}
