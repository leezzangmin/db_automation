package zzangmin.db_automation.dto.request.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MysqlPrivilegeGrantRequestDTO implements AccountRequestDTO {

    private String accountName; // 'test_account'@'127.0.0.1'
    private List<String> privileges; // SELECT, INSERT
    private String target; // ON test_db.*

    @Override
    public DatabaseRequestCommandGroup.CommandType extractCommandType() {
        return DatabaseRequestCommandGroup.CommandType.GRANT_PRIVILEGE;
    }

    @Override
    public String toSQL() {
        if (privileges == null || privileges.size() == 0) {
            throw new IllegalArgumentException("부여하려는 권한이 없습니다.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("GRANT ");
        for (String privilege : privileges) {
            sb.append(privilege);
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));

        sb.append(" ON");
        sb.append(target);
        sb.append(" TO ");
        sb.append(accountName);

        return sb.toString();
    }
}
