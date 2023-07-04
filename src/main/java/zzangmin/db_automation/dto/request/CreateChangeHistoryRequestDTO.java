package zzangmin.db_automation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.CommandType;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChangeHistoryRequestDTO {
    private CommandType commandType;
    private String databaseIdentifier;
    private String schemaName;
    private String tableName;
    private String doer;
    private LocalDateTime doDateTime;
    private String changeeeeeeeee;

}
