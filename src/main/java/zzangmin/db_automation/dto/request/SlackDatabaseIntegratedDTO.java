package zzangmin.db_automation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SlackDatabaseIntegratedDTO {
    private DatabaseConnectionInfo databaseConnectionInfo;
    private String requestUserSlackId;
    private DatabaseRequestCommandGroup.CommandType commandType;
    private String requestDTOClassType;
    private RequestDTO requestDTO;
    private String requestUUID;
    private String requestContent;
    private String requestDescription;

}
