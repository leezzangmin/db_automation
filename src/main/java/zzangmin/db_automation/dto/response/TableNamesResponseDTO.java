package zzangmin.db_automation.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class TableNamesResponseDTO {
    private String databaseIdentifier;
    private String schemaName;
    private List<String> tableNames;
}
