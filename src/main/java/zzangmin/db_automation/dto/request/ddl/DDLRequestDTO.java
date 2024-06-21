package zzangmin.db_automation.dto.request.ddl;

import zzangmin.db_automation.dto.request.RequestDTO;

public interface DDLRequestDTO extends RequestDTO {
    String toSQL();
}
