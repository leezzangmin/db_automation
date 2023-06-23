package zzangmin.db_automation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.Column;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddColumnRequestDTO extends DDLRequestDTO {
      private String schemaName;
      private String tableName;
      private Column column;

}
