package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RenameColumnResponseDTO extends ResponseDTO {


    // 실행자 ex) ckdals1234@gmail.com
    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private String tableName;
    private String createStatement;

    @Override
    public String toString() {
        String executeDuration = super.getExecuteDuration();
        return "RenameColumnResponseDTO{" +
                "executeDoer='" + executeDoer + '\'' +
                ", databaseInstanceName='" + databaseInstanceName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", createStatement='" + createStatement + '\'' +
                ", executeDuration='" + executeDuration + '\'' +
                '}';
    }
}
