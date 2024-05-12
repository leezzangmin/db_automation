package zzangmin.db_automation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTableRequestDTO extends DDLRequestDTO {

    @NotBlank
    private String schemaName;
    @NotBlank
    private String tableName;
    @NotBlank
    private Set<Column> columns;
    @NotBlank
    private Set<Constraint> constraints;
    @NotBlank
    private String engine;
    @NotBlank
    private String charset;
    @NotBlank
    private String collate;
    @NotBlank
    private String tableComment;

    // TODO
//    public static CreateTableRequestDTO of(String createTableSQL) {
//        // parse SQL String
//
//        String schemaName;
//        String tableName;
//        List<Column> columns = new ArrayList<>();
//        List<Constraint> constraints = new ArrayList<>();
//        String engine;
//        String charset;
//        String collate;
//        String tableComment;
//        String lowerCaseSQL = createTableSQL.replaceAll("\n", " ")
//                .replaceAll("`", "")
//                .replaceAll("  ", " ")
//                .toLowerCase();
//
//        return new CreateTableRequestDTO(schemaName, tableName, columns, constraints, engine, charset, collate, tableComment);
//    }
}
