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
    private Long monitorTargetDatabaseId;
    private String requestUserSlackId;
    @Enumerated(EnumType.STRING)
    private DatabaseRequestCommandGroup.CommandType commandType;
    private String requestDTOClassType;
    private String requestDTO;
    private String requestUUID;
    private String requestSQL;
    private String requestDescription;
    private LocalDateTime requestDatetime;
    private Boolean isComplete;
}
