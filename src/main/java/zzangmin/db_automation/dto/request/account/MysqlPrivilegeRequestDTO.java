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
public class MysqlPrivilegeRequestDTO implements AccountRequestDTO {
    @NotBlank
    private String accountName;

    @Override
    public DatabaseRequestCommandGroup.CommandType getCommandType() {
        return DatabaseRequestCommandGroup.CommandType.SHOW_GRANTS;
    }
}
