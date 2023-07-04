package zzangmin.db_automation.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class RdsClusterSchemaTablesResponseDTO {

    private String schemaName;
    private List<TableInfo> tableInfos = new ArrayList<>();


    @ToString
    @Getter
    @AllArgsConstructor
    public static class TableInfo {
        private String tableName;
        private long tableSizeByte;
        private int tableRowCount;
    }

}
