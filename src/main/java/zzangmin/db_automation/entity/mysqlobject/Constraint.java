package zzangmin.db_automation.entity.mysqlobject;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.sf.jsqlparser.statement.create.table.Index;
import zzangmin.db_automation.convention.CommonConvention;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Constraint {
    @NotBlank
    private ConstraintType constraintType; // PRIMARY KEY, UNIQUE KEY, KEY
    @NotBlank
    private String keyName; // promotion_type_date_promotion_end
    @NotBlank
    private List<String> keyColumnNames = new ArrayList<>(); // promotion_type, date_promotion_end;

    public void addKeyColumnNames(List<String> keyColumnNames) {
        this.keyColumnNames.addAll(keyColumnNames);
    }


    public String reportDifference(Constraint otherConstraint) {
        StringBuilder differences = new StringBuilder();
        if (!this.constraintType.equals(otherConstraint.constraintType)) {
            differences.append(String.format("제약조건 타입이 다릅니다: `%s` <-> `%s`\n", this.constraintType.typeName, otherConstraint.constraintType.typeName));
        }
        if (!this.keyName.equals(otherConstraint.keyName)) {
            differences.append(String.format("제약조건 이름이 다릅니다: `%s` <-> `%s`\n", this.keyName, otherConstraint.keyName));
        }
        if (!this.keyColumnNames.equals(otherConstraint.keyColumnNames)) {
            differences.append(String.format("제약조건 컬럼 구성이 다릅니다: `%s` <-> `%s`\n", this.keyColumnNames, otherConstraint.keyColumnNames));
        }
        return differences.toString();
    }

    public static Constraint of(Index index) {
        Constraint constraint = Constraint.builder()
                .constraintType(Constraint.ConstraintType.generateConstraintTypeByTypeName(index.getType()))
                .keyName(index.getName())
                .keyColumnNames(index.getColumnsNames())
                .build();
        return constraint;
    }


    public enum ConstraintType {

        PRIMARY("PRIMARY KEY"),
        UNIQUE("UNIQUE KEY"),
        KEY("KEY");

        public final String typeName;

        ConstraintType(String typeName) {
            if (!CommonConvention.ALLOWED_CONSTRAINT_TYPE.contains(typeName)) {
                throw new IllegalArgumentException("허용된 Constraint Type 이 아닙니다. [" + typeName + "], 허용된 타입: " + CommonConvention.ALLOWED_CONSTRAINT_TYPE);
            }
            this.typeName = typeName;
        }
        public static ConstraintType generateConstraintTypeByTypeName(String typeName) {
            ConstraintType[] constraintTypes = ConstraintType.values();
            for (ConstraintType constraintType : constraintTypes) {
                if (constraintType.typeName.equals(typeName)) {
                    return constraintType;
                }
            }
            throw new IllegalArgumentException("index typeName 이 존재하지 않음");
        }
    }

}
