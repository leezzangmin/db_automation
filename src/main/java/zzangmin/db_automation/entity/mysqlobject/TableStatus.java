package zzangmin.db_automation.entity.mysqlobject;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class TableStatus {

    private String schemaName;
    private String tableName;
    private String tableType;
    private String tableEngine;
    private int tableRow;
    private long dataLength;
    private long indexLength;
    private String createTime;
    private String updateTime;

    public long calculateTotalTableByteSize() {
        long totalTableByteSize = this.dataLength + this.indexLength;
        return totalTableByteSize;
    }
}
