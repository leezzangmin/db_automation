package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class SchemaNamesResponseDTO {

    private String databaseIdentifier;
    private List<String> schemaNames;
}
