package zzangmin.db_automation.dto.request.account;

import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

public interface AccountRequestDTO extends RequestDTO {
    DatabaseRequestCommandGroup.CommandType getCommandType();
}
