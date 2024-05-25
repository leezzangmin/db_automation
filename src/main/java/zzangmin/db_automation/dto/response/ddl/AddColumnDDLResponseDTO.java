package zzangmin.db_automation.dto.response.ddl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddColumnDDLResponseDTO extends DDLResponseDTO {

    // 실행자 ex) ckdals1234@gmail.com
    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String createStatement;

    @Override
    public String toString() {
        String executeDuration = super.getExecuteDuration();
        return "AddColumnResponseDTO{" +
                "executeDoer='" + executeDoer + '\'' +
                ", databaseInstanceName='" + databaseInstanceName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", createStatement='" + createStatement + '\'' +
                ", executeDuration='" + executeDuration + '\'' +
                '}';
    }
}
