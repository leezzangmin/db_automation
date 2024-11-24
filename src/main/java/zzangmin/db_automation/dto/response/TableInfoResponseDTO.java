package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import zzangmin.db_automation.entity.ChangeHistory;
import zzangmin.db_automation.entity.mysqlobject.Column;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class TableInfoResponseDTO {

    private String databaseIdentifier;
    private String schemaName;
    private String tableName;
    private List<Column> columns;

    private List<ChangeHistory> changeHistories;
}
