package zzangmin.db_automation.dto.response.dml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class SelectQueryResponseDTO {
    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private int recordCount;
    private String resultJson;
}
