package zzangmin.db_automation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class Constraint {
    private String type; // PRIMARY KEY, UNIQUE KEY, KEY
    private String keyName; // promotion_type_date_promotion_end
    private List<String> keyColumnNames; // (promotion_type, date_promotion_end);
}
