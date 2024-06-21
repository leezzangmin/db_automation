package zzangmin.db_automation.dto.response.dml;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SelectQueryResponseDTO {
    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private String resultJson;
}
