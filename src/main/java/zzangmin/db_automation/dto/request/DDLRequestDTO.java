package zzangmin.db_automation.dto.request;

import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

public interface DDLRequestDTO {

    String toSQL();
    DatabaseRequestCommandGroup.CommandType getCommandType();
}
