package zzangmin.db_automation.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType_old;
import zzangmin.db_automation.entity.Constraint;

import java.util.Set;

@Slf4j
@Component
public class DDLParser {

    public String commandToSql(DDLRequestDTO ddlRequestDTO) {
        if (ddlRequestDTO.getCommandType().equals(CommandType_old.ADD_COLUMN)) {
            return addColumnCommandToSql((AddColumnRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.ALTER_COLUMN)) {
            return alterColumnCommandToSql((AlterColumnRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.CREATE_INDEX)) {
            return createIndexCommandToSql((CreateIndexRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.CREATE_TABLE)) {
            return createTableCommandToSql((CreateTableRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.DELETE_COLUMN)) {
            return deleteColumnCommandToSql((DeleteColumnRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType_old.RENAME_COLUMN)) {
            return renameColumnCommandToSql((RenameColumnRequestDTO) ddlRequestDTO);
        }
        throw new IllegalArgumentException("존재하지 않는 명령입니다.");
    }

    private String renameColumnCommandToSql(RenameColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` RENAME COLUMN `");
        sb.append(dto.getBeforeColumnName());
        sb.append("` TO `");
        sb.append(dto.getAfterColumnName());
        sb.append("`");
        return sb.toString();
    }

    private String alterColumnCommandToSql(AlterColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` MODIFY COLUMN `");
        sb.append(dto.getTargetColumnName());
        sb.append("` ");
        sb.append(dto.getAfterColumn().getType());
        sb.append(" ");
        sb.append(dto.getAfterColumn().generateNull());
        // sb.append(dto.getAfterColumn().generateUnique());
        sb.append(dto.getAfterColumn().generateAutoIncrement());
        sb.append(" COMMENT '");
        sb.append(dto.getAfterColumn().getComment());
        sb.append("'");
        return sb.toString();
    }

    private String deleteColumnCommandToSql(DeleteColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` DROP COLUMN `");
        sb.append(dto.getColumnName());
        sb.append("`");
        return sb.toString();
    }

    public String extendVarcharColumnCommandToSql(ExtendVarcharColumnRequestDTO dto, Column afterColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` MODIFY COLUMN `");
        sb.append(afterColumn.getName());
        sb.append("` ");
        sb.append(afterColumn.getType());
        sb.append(" ");
        sb.append(afterColumn.generateNull());
        // sb.append(afterColumn.generateUnique());
        sb.append(afterColumn.generateAutoIncrement());
        sb.append(" COMMENT '");
        sb.append(afterColumn.getComment());
        sb.append("'");
        return sb.toString();
    }

    private String addColumnCommandToSql(AddColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` ADD COLUMN `");
        sb.append(dto.getColumn().getName());
        sb.append("` ");
        sb.append(dto.getColumn().getType());
        sb.append(" ");
        sb.append(dto.getColumn().generateNull());
        // sb.append(dto.getColumn().generateUnique());
        sb.append(" COMMENT '");
        sb.append(dto.getColumn().getComment());
        sb.append("'");
        return sb.toString();
    }

    private String createIndexCommandToSql(CreateIndexRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` ADD INDEX `");
        sb.append(dto.getIndexName());
        sb.append("` (");
        for (String columnName : dto.getColumnNames()) {
            sb.append("`");
            sb.append(columnName);
            sb.append("`,");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        return sb.toString();
    }

    private String createTableCommandToSql(CreateTableRequestDTO dto) {
        StringBuilder sb = new StringBuilder();

        Set<Column> columns = dto.getColumns();
        Set<Constraint> constraints = dto.getConstraints();
        sb.append(generateCreateTableStatement(dto.getSchemaName(), dto.getTableName()));
        sb.append(generateColumnStatement(columns));
        sb.append(generateConstraintStatement(constraints));
        sb.append(generateCreateTableOptions(dto.getEngine(), dto.getCharset(), dto.getCollate(), dto.getTableComment()));
        return sb.toString();
    }

    private String generateCreateTableStatement(String schemaName, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `");
        sb.append(schemaName);
        sb.append("`.`");
        sb.append(tableName);
        sb.append("` (\n");
        return sb.toString();
    }

    private String generateColumnStatement(Set<Column> columns) {
        StringBuilder sb = new StringBuilder();
        for (Column column : columns) {
            sb.append("`");
            sb.append(column.getName());
            sb.append("` ");
            sb.append(column.getType());
            sb.append(" ");
            sb.append(column.generateNull());
            // sb.append(column.generateUnique());
            sb.append(column.generateAutoIncrement());
            sb.append(" COMMENT '");
            sb.append(column.getComment());
            sb.append("',\n");
        }
        if (sb.isEmpty()) {
            return sb.toString();
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    private String generateConstraintStatement(Set<Constraint> constraints) {
        StringBuilder sb = new StringBuilder();
        for (Constraint constraint : constraints) {
            sb.append(constraint.getConstraintType().typeName);
            if (!constraint.getConstraintType().equals(Constraint.ConstraintType.PRIMARY)) {
                sb.append(" `");
                sb.append(constraint.getKeyName());
                sb.append("`");
            }
            sb.append(" (");
            for (String keyName : constraint.getKeyColumnNames()) {
                sb.append("`");
                sb.append(keyName);
                sb.append("`,");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("),\n");
        }
        log.info("sbtostring: {}", sb.toString());
        if (sb.isEmpty()) {
            return sb.toString();
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    private String generateCreateTableOptions(String engine, String charset, String collate, String comment) {
        StringBuilder sb = new StringBuilder();
        sb.append(") ENGINE=");
        sb.append(engine);
        sb.append(" DEFAULT CHARSET=");
        sb.append(charset);
        sb.append(" COLLATE=");
        sb.append(collate);
        sb.append(" COMMENT='");
        sb.append(comment);
        sb.append("'");
        return sb.toString();
    }
}
