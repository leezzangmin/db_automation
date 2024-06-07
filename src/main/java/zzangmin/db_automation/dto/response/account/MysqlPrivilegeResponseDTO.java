package zzangmin.db_automation.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class MysqlPrivilegeResponseDTO {
    private String accountName; // 'test_account'@'10.1.0.0/255.255.254.0'
    private List<String> privileges; // GRANT AUDIT_ABORT_EXEMPT,FIREWALL_EXEMPT,SYSTEM_USER ON *.* TO `mysql.sys`@`localhost`

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("계정명: ");
        sb.append(this.accountName);
        sb.append("\n");
        sb.append("권한목록: \n");
        for (String privilege : this.privileges) {
            sb.append(privilege);
            sb.append(";\n");
        }
        return sb.toString();
    }
}
