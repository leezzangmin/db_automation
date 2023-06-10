package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class CreateTableResponseDTO extends ResponseDTO {

    // 실행자 ex) ckdals1234@gmail.com
    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private String tableName;
    private String createStatement;
}
