package zzangmin.db_automation.convention;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.entity.Constraint;
import zzangmin.db_automation.info.DatabaseConnectionInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static zzangmin.db_automation.convention.CommonConvention.CONSTRAINT_TYPE;

@RequiredArgsConstructor
@Component
public class IndexConvention {

    // https://dev.mysql.com/doc/refman/5.7/en/create-index.html
    private final CommonConvention commonConvention;

    public void validateIndexConvention(Constraint constraint) {
        if (!constraint.getType().equals("PRIMARY KEY")) {
            commonConvention.validateSnakeCase(constraint.getKeyName());
            validateConstraintNamingConvention(constraint.getKeyName(), constraint.getKeyColumnNames());
        }
        validateDuplicateColumnConvention(constraint.getKeyColumnNames());
        checkConstraintType(constraint.getType());
    }

    public void validateConstraintNamingConvention(String indexName, List<String> columnNames) {
        if (!String.join("_", columnNames).equals(indexName)) {
            throw new IllegalArgumentException(indexName + " 키 이름이 컬럼을 '_' 으로 이어붙인 형식이 아닙니다.");
        }
    }

    private void validateDuplicateColumnConvention(List<String> columnNames) {
        Set<String> columnNameSet = new HashSet<>();
        for (String columnName : columnNames) {
            if (columnNameSet.contains(columnName)) {
                throw new IllegalArgumentException("중복된 컬럼명이 존재합니다: " + columnName);
            }
            columnNameSet.add(columnName);
        }
    }

    private void checkConstraintType(String constraintType) {
        if (!CONSTRAINT_TYPE.contains(constraintType)) {
            throw new IllegalArgumentException("허용된 Constraint Type 이 아닙니다. [" + constraintType + "], 허용된 타입: " + CONSTRAINT_TYPE);
        }
    }

}
