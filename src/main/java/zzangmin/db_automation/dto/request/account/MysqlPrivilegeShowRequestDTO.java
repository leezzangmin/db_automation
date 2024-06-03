package zzangmin.db_automation.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MysqlPrivilegeShowRequestDTO implements AccountRequestDTO {
    @NotBlank
    private String accountName; // test_account@10.199.0.0/255.255.254.0

    @Override
    public DatabaseRequestCommandGroup.CommandType extractCommandType() {
        return DatabaseRequestCommandGroup.CommandType.SHOW_GRANTS;
    }

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("SHOW GRANTS FOR ");
        sb.append(this.getAccountName());
        return sb.toString();
    }
}
