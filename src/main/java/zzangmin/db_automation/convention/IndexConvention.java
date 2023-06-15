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

    /**
     * 1. 인덱스 네이밍 컨벤션
     * 2. 중복된 옵션 존재 여부 (동일 column 입력, 동일 이름 constraint 및 동일 속성 존재여부)
     * 3.
     */
    public void validateIndexConvention(Constraint constraint) {
        commonConvention.validateSnakeCase(constraint.getKeyName());
        validateDuplicateColumnConvention(constraint.getKeyColumnNames());
        validateConstraintNamingConvention(constraint.getKeyName(), constraint.getKeyColumnNames());
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
