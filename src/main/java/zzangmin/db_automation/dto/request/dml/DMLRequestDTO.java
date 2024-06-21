package zzangmin.db_automation.dto.request.dml;

import zzangmin.db_automation.dto.request.RequestDTO;

public interface DMLRequestDTO extends RequestDTO {
    String toSQL();
}
