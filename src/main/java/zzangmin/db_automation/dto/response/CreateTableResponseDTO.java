package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateTableResponseDTO {

    // 소요시간
    private double executeDuration;
    // 실행자 ex) 이창민
    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private String tableName;
    private String createString;
}
