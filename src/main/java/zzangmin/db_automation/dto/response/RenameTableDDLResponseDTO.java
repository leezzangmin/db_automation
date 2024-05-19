package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RenameTableDDLResponseDTO extends DDLResponseDTO {

    private String executeDoer;
    private String databaseInstanceName;
    private String schemaName;
    private String oldTableName;
    private String newTableName;
    private String createStatement;

    @Override
    public String toString() {
        String executeDuration = super.getExecuteDuration();
        return "RenameTableDDLResponseDTO{" +
                "executeDoer='" + executeDoer + '\'' +
                ", databaseInstanceName='" + databaseInstanceName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", oldTableName='" + oldTableName + '\'' +
                ", newTableName='" + newTableName + '\'' +
                ", createStatement='" + createStatement + '\'' +
                ", executeDuration='" + executeDuration + '\'' +
                '}';
    }
}
