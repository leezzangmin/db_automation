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
public class ChangeHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CommandType_old commandType;
    private String databaseIdentifier;
    private String schemaName;
    private String tableName;
    private String doer;
    private LocalDateTime doDateTime;
    private String changeContent;

}
