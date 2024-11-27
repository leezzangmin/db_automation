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
public class SlackDatabaseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_target_database_id", nullable = false)
    private MonitorTargetDatabase monitorTargetDatabase;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slack_user_id", nullable = false)
    private SlackUser slackUser;
    @Enumerated(EnumType.STRING)
    private DatabaseRequestCommandGroup.CommandType commandType;
    private String requestDtoClassType;
    private String requestDto;
    private String requestUuid;
    private String requestContent;
    private String requestDescription;
    private LocalDateTime requestDatetime;
    private LocalDateTime executeDatetime;
    @Enumerated(EnumType.STRING)
    private ExecuteStatus executeStatus;

    public enum ExecuteStatus {
        VOTING,
        IN_PROGRESS,
        DENIED,
        COMPLETE
    }

    public void complete() {
        this.executeStatus = ExecuteStatus.COMPLETE;
    }

    public void accept() {
        this.executeStatus = ExecuteStatus.IN_PROGRESS;
    }

    public void deny() {
        this.executeStatus = ExecuteStatus.DENIED;
    }

    public boolean isVotableStatus() {
        return this.executeStatus == ExecuteStatus.VOTING;
    }
}
