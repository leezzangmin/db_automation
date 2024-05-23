package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class MysqlPrivilegeResponseDTO {
    private String accountName; // 'test_account'@'10.1.0.0/255.255.254.0'
    private List<String> privileges;
}
