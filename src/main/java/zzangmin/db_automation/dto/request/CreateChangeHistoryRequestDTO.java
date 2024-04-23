package zzangmin.db_automation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.CommandType_old;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChangeHistoryRequestDTO {
    private CommandType_old commandType;
    private String databaseIdentifier;
    private String schemaName;
    private String tableName;
    private String doer;
    private LocalDateTime doDateTime;

}
