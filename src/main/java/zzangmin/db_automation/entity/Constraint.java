package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Constraint {
    @NotBlank
    private String type; // PRIMARY KEY, UNIQUE KEY, KEY
    @NotBlank
    private String keyName; // promotion_type_date_promotion_end
    @NotBlank
    private List<String> keyColumnNames; // promotion_type, date_promotion_end;
}
