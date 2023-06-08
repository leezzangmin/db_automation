package zzangmin.db_automation.dto;

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
public class CreateTableRequestDTO implements DDLRequestDTO{
    private CommandType commandType;
    private String schemaName;
    private String tableName;
    private List<Column> columns;
    private List<Constraint> constraints;
    private String engine;
    private String charset;
    private String collate;
    private String tableComment;
}
