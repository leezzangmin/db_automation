package zzangmin.db_automation.dto.request.ddl;

import zzangmin.db_automation.dto.request.RequestDTO;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

public interface DDLRequestDTO extends RequestDTO {

    String toSQL();
    DatabaseRequestCommandGroup.CommandType extractCommandType();
}
