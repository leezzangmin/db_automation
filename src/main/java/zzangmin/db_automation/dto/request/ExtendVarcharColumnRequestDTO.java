package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExtendVarcharColumnRequestDTO extends DDLRequestDTO {

    @NotBlank
    private String schemaName;
    @NotBlank
    private String tableName;
    @NotBlank
    private String targetColumnName;
    @NotBlank
    private int extendSize;
}
