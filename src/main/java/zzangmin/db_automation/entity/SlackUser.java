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
public class SlackUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userSlackId;
    private String userSlackName;
    @Enumerated(EnumType.STRING)
    private SlackUserType userType;
    private LocalDateTime expireDatetime;

    public enum SlackUserType {
        ADMIN,
        NORMAL,
    }
}
