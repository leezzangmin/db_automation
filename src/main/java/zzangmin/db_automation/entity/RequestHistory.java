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
public class RequestHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private DatabaseRequestCommandGroup.CommandType commandType;
    private String command;
    private String requestDoer;
    private String databaseIdentifier;
    private String schemaName;
    private String tableName;
    private LocalDateTime requestDatetime;
    private LocalDateTime performDatetime;
    private double executionDuration;
}
