package zzangmin.db_automation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class CreateTableRequestDTO {
    private String schemaName;
    private String tableName;
    private List<Column> columns;
    private String engine;
    private String charset;
    private String collate;
    private String tableComment;
}
