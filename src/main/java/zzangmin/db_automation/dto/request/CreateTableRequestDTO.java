package zzangmin.db_automation.dto.request;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.service.SlackService;
import zzangmin.db_automation.slackview.SlackConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
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
    public static CreateTableRequestDTO of(String createTableSQL) throws JSQLParserException {
        CreateTable parse = (CreateTable) CCJSqlParserUtil.parse(createTableSQL);

        Set<Constraint> constraints = new HashSet<>();
        List<Index> indexes = parse.getIndexes();
        if (indexes != null) {
            for (Index index : indexes) {
                constraints.add(Constraint.of(index));
            }
        }

        List<ColumnDefinition> columnDefinitions = parse.getColumnDefinitions();
        Set<Column> columns = new HashSet<>();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columns.add(Column.of(columnDefinition));
            List<String> columnSpecs = columnDefinition.getColumnSpecs();
            if (columnSpecs.contains("primary") || columnSpecs.contains("PRIMARY")) {
                constraints.add(Constraint.builder()
                        .constraintType(Constraint.ConstraintType.PRIMARY)
                        .keyName(columnDefinition.getColumnName())
                        .build()
                );
            } else if (columnSpecs.contains("unique") || columnSpecs.contains("UNIQUE")) {
                constraints.add(Constraint.builder()
                        .constraintType(Constraint.ConstraintType.UNIQUE)
                        .keyName(columnDefinition.getColumnName())
                        .build()
                );
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
        if (tableOptionsStrings.indexOf("comment") != -1) {
            tableCommentOptionIndex = tableOptionsStrings.indexOf("comment");
        } else if (tableOptionsStrings.indexOf("COMMENT") != -1) {
            tableCommentOptionIndex = tableOptionsStrings.indexOf("COMMENT");
        }
        String tableComment;
        try {
            tableComment = tableOptionsStrings.get(tableCommentOptionIndex + 2).replace("'","");
        } catch (Exception e) {
            tableComment = tableOptionsStrings.get(tableCommentOptionIndex + 1).replace("'","");
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
