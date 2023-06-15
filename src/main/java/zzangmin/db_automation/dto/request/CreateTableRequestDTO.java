package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.entity.Constraint;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class CreateTableRequestDTO extends DDLRequestDTO {

    @NotBlank
    private CommandType commandType;
    @NotBlank
    private String schemaName;
    @NotBlank
    private String tableName;
    @NotBlank
    private List<Column> columns;
    @NotBlank
    private List<Constraint> constraints;
    @NotBlank
    private String engine;
    @NotBlank
    private String charset;
    @NotBlank
    private String collate;
    @NotBlank
    private String tableComment;
}
