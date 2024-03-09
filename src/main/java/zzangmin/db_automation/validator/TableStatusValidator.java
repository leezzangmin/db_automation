package zzangmin.db_automation.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.entity.TableStatus;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;

import static zzangmin.db_automation.convention.CommonConvention.TABLE_BYTE_SIZE_THRESHOLD;
import static zzangmin.db_automation.convention.CommonConvention.TABLE_ROW_SIZE_THRESHOLD;

@RequiredArgsConstructor
@Component
public class TableStatusValidator {

    private final MysqlClient mysqlClient;

    public void validateTableSize(DatabaseConnectionInfo databaseConnectionInfo, String schemaName, String tableName) {
        TableStatus tableStatus = mysqlClient.findTableStatus(databaseConnectionInfo, schemaName, tableName);
        long totalTableByteSize = tableStatus.calculateTotalTableByteSize();
        if (totalTableByteSize > TABLE_BYTE_SIZE_THRESHOLD) {
            throw new IllegalStateException("테이블 사이즈가 커서 실행이 불가합니다. 현재 사이즈: " + totalTableByteSize);
        }
        if (tableStatus.getTableRow() > TABLE_ROW_SIZE_THRESHOLD) {
            throw new IllegalStateException("테이블 row 가 커서 실행이 불가합니다. 현재 row size: " + tableStatus.getTableRow());
        }
    }
}
