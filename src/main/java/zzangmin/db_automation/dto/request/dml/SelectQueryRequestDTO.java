package zzangmin.db_automation.dto.request.dml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;


@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectQueryRequestDTO implements DMLRequestDTO {

    private String schemaName;
    private String SQL;

    @Override
    public String toSQL() {
        return SQL;
    }

    @Override
    public DatabaseRequestCommandGroup.CommandType extractCommandType() {
        return DatabaseRequestCommandGroup.CommandType.SELECT;
    }


}
