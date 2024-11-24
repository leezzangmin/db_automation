package zzangmin.db_automation.dto.request.ddl;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import zzangmin.db_automation.entity.mysqlobject.Column;
import zzangmin.db_automation.entity.mysqlobject.Constraint;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;
import zzangmin.db_automation.parser.DDLParser;

import java.util.*;

@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTableRequestDTO implements DDLRequestDTO {

    @NotBlank
    private String schemaName;
    @NotBlank
    private String tableName;
    @NotBlank
    private LinkedHashSet<Column> columns;
    @NotBlank
    private LinkedHashSet<Constraint> constraints;
    @NotBlank
    private String engine;
    @NotBlank
    private String charset;
    @NotBlank
    private String collate;
    @NotBlank
    private String tableComment;

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();

        Set<Column> columns = this.getColumns();
        Set<Constraint> constraints = this.getConstraints();
        sb.append(DDLParser.generateCreateTableStatement(this.getSchemaName(), this.getTableName()));
        sb.append(DDLParser.generateColumnStatement(columns));
        sb.append(DDLParser.generateConstraintStatement(constraints));
        sb.append(DDLParser.generateCreateTableOptions(this.getEngine(),
                this.getCharset(),
                this.getCollate(),
                this.getTableComment()));
        return sb.toString();
    }

    @Override
    public DatabaseRequestCommandGroup.CommandType extractCommandType() {
        return DatabaseRequestCommandGroup.CommandType.CREATE_TABLE;
    }

    public static CreateTableRequestDTO of(String createTableSQL) throws JSQLParserException {
        createTableSQL = createTableSQL.replaceAll("`", "");
        CreateTable parse = (CreateTable) CCJSqlParserUtil.parse(createTableSQL);

        LinkedHashSet<Constraint> constraints = new LinkedHashSet<>();
        List<Index> indexes = parse.getIndexes();
        if (indexes != null) {
            for (Index index : indexes) {
                constraints.add(Constraint.of(index));
            }
        }

        List<ColumnDefinition> columnDefinitions = parse.getColumnDefinitions();
        LinkedHashSet<Column> columns = new LinkedHashSet<>();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columns.add(Column.of(columnDefinition));
            List<String> columnSpecs = columnDefinition.getColumnSpecs();
            if (columnSpecs.contains("primary") || columnSpecs.contains("PRIMARY")) {
                Optional<Constraint> pkConstraint = constraints.stream()
                        .filter(c -> c.getConstraintType().equals(Constraint.ConstraintType.PRIMARY))
                        .findAny();
                if (pkConstraint.isEmpty()) {
                    constraints.add(Constraint.builder()
                            .constraintType(Constraint.ConstraintType.PRIMARY)
                            .keyName(columnDefinition.getColumnName())
                            .keyColumnNames(List.of(columnDefinition.getColumnName()))
                            .build()
                    );
                }
            } else if (columnSpecs.contains("unique") || columnSpecs.contains("UNIQUE")) {
                Optional<Constraint> ukConstraint = constraints.stream()
                        .filter(c -> c.getConstraintType().equals(Constraint.ConstraintType.PRIMARY))
                        .filter(c -> c.getKeyColumnNames().size() == 1)
                        .filter(c -> c.getKeyColumnNames().get(0).equals(columnDefinition.getColumnName()))
                        .findAny();
                if (ukConstraint.isEmpty()) {
                    constraints.add(Constraint.builder()
                            .constraintType(Constraint.ConstraintType.UNIQUE)
                            .keyName(columnDefinition.getColumnName())
                            .keyColumnNames(List.of(columnDefinition.getColumnName()))
                            .build()
                    );
                }
            }
        }

        List<String> tableOptionsStrings = parse.getTableOptionsStrings();
        if (tableOptionsStrings == null) {
            throw new IllegalArgumentException("table option 이 null 입니다. ex) engine, charset, collate, comment 등 ");
        }
        int engineOptionIndex = -1;
        if (tableOptionsStrings.indexOf("engine") != -1) {
            engineOptionIndex = tableOptionsStrings.indexOf("engine");
        } else if (tableOptionsStrings.indexOf("ENGINE") != -1) {
            engineOptionIndex = tableOptionsStrings.indexOf("ENGINE");
        }
        int charsetOptionIndex = -1;
        if (tableOptionsStrings.indexOf("charset") != -1) {
            charsetOptionIndex = tableOptionsStrings.indexOf("charset");
        } else if (tableOptionsStrings.indexOf("CHARSET") != -1) {
            charsetOptionIndex = tableOptionsStrings.indexOf("CHARSET");
        }
        int collateOptionIndex = -1;
        if (tableOptionsStrings.indexOf("collate") != -1) {
            collateOptionIndex = tableOptionsStrings.indexOf("collate");
        } else if (tableOptionsStrings.indexOf("COLLATE") != -1) {
            collateOptionIndex = tableOptionsStrings.indexOf("COLLATE");
        }
        int tableCommentOptionIndex = -1;
        log.info("tableOptionsStrings: {}", tableOptionsStrings);
        if (tableOptionsStrings.indexOf("comment") != -1) {
            tableCommentOptionIndex = tableOptionsStrings.indexOf("comment");
        } else if (tableOptionsStrings.indexOf("COMMENT") != -1) {
            tableCommentOptionIndex = tableOptionsStrings.indexOf("COMMENT");
        }
        if (tableCommentOptionIndex == -1 ) {
            throw new IllegalArgumentException("table Comment 가 없습니다.");
        }

        String tableComment;
        try {
            tableComment = tableOptionsStrings.get(tableCommentOptionIndex + 2).replace("'", "");
        } catch (Exception e) {
            tableComment = tableOptionsStrings.get(tableCommentOptionIndex + 1).replace("'", "");
        }

        CreateTableRequestDTO createTableRequestDTO = new CreateTableRequestDTO(parse.getTable().getSchemaName(),
                parse.getTable().getName(),
                columns,
                constraints,
                engineOptionIndex == -1 ? null : tableOptionsStrings.get(engineOptionIndex + 2),
                charsetOptionIndex == -1 ? null : tableOptionsStrings.get(charsetOptionIndex + 2),
                collateOptionIndex == -1 ? null : tableOptionsStrings.get(collateOptionIndex + 2),
                tableComment
        );
        log.info("jsqlParser createTableRequestDTO: {}", createTableRequestDTO);
        return createTableRequestDTO;
    }

}
