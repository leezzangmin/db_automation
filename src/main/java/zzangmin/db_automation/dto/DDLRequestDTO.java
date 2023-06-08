package zzangmin.db_automation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.CommandType;

@ToString
@Getter
@NoArgsConstructor
public class DDLRequestDTO {
    private CommandType commandType;
}
