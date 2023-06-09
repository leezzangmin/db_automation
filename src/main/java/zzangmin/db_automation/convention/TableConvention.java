package zzangmin.db_automation.convention;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.request.CreateTableRequestDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.Constraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TableConvention {

    /**
     * 0. 테이블, 컬럼, 인덱스 등 네이밍 컨벤션 (+영어만 허용, 공백X, snake_case)
     * 1. 중복된 옵션(column, constraint) 있는지
     * 2. 테이블 생성 컨벤션 (engine charset, comment 등)
     */
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z]+(_[a-z]+)*$");
    private static final Pattern PURE_LOWER_CASE_PATTER = Pattern.compile("^[a-z_]+$");
    private static final String ENGINE_TYPE = "InnoDB";
    private static final String CHARSET = "utf8mb4";
    private static final String COLLATE = "utf8mb4_general_ci";
    private static final Set<String> CONSTRAINT_TYPE = Set.of("PRIMARY KEY", "UNIQUE KEY", "KEY");

    public void validateTableConvention(CreateTableRequestDTO createTableRequestDTO) {
        checkDuplicateColumnAndConstraintConvention(createTableRequestDTO.getColumns(), createTableRequestDTO.getConstraints());
        checkNamingConvention(createTableRequestDTO);
        checkColumnCommentExistConvention(createTableRequestDTO);
        checkTableOptionConvention(createTableRequestDTO);
        checkConstraintType(createTableRequestDTO.getConstraints());
    }

    private void checkNamingConvention(CreateTableRequestDTO createTableRequestDTO) {
        List<Column> columns = createTableRequestDTO.getColumns();
        List<Constraint> constraints = createTableRequestDTO.getConstraints();
        if (!isSnakeCase(createTableRequestDTO.getTableName())) {
            throw new IllegalArgumentException("테이블명이 snake_case 가 아닙니다.");
        } else if (!isLowerCaseString(createTableRequestDTO.getTableName())) {
            throw new IllegalArgumentException("테이블명에 불필요한 문자가 포함되어 있습니다.");
        }
        for (Column column : columns) {
            if (!isSnakeCase(column.getName())) {
                throw new IllegalArgumentException(column.getName() + "컬럼이 snake_case 가 아닙니다.");
            } else if (!isLowerCaseString(column.getName())) {
                throw new IllegalArgumentException(column.getName() + "컬럼에 불필요한 문자가 포함되어 있습니다.");
            }
        }
        for (Constraint constraint : constraints) {
            if (constraint.getType().equals("PRIMARY KEY")) {
                continue;
            }
            if (!isSnakeCase(constraint.getKeyName())) {
                throw new IllegalArgumentException(constraint.getKeyName() + " 키 이름이 snake_case 가 아닙니다.");
            } else if (!isLowerCaseString(constraint.getKeyName())) {
                throw new IllegalArgumentException(constraint.getKeyName() + " 키 이름에 불필요한 문자가 포함되어 있습니다.");
            } else if (!String.join("_", constraint.getKeyColumnNames()).equals(constraint.getKeyName())) {
                throw new IllegalArgumentException(constraint.getKeyName() + " 키 이름이 컬럼을 '_' 으로 이어붙인 형식이 아닙니다.");
            }
        }
    }

    private void checkColumnCommentExistConvention(CreateTableRequestDTO createTableRequestDTO) {
        List<Column> columns = createTableRequestDTO.getColumns();
        for (Column column : columns) {
            if (column.getComment().isBlank() || column.getComment().isEmpty()) {
                throw new IllegalArgumentException("테이블 코멘트가 존재하지 않습니다.");
            }
        }
    }

    private void checkDuplicateColumnAndConstraintConvention(List<Column> columns, List<Constraint> constraints) {
        Set<String> columnNames = new HashSet<>();
        for (Column column : columns) {
            String columnName = column.getName();
            if (columnNames.contains(columnName)) {
                throw new IllegalArgumentException("중복된 컬럼명이 존재합니다: " + columnName);
            }
            columnNames.add(columnName);
        }

        Set<String> constraintNames = new HashSet<>();
        for (Constraint constraint : constraints) {
            String constraintName = constraint.getKeyName();
            if (constraintNames.contains(constraintName)) {
                throw new IllegalArgumentException("중복된 키 이름이 존재합니다: " + constraintName);
            }
            constraintNames.add(constraintName);
        }
    }

    private void checkTableOptionConvention(CreateTableRequestDTO createTableRequestDTO) {
        if (!createTableRequestDTO.getEngine().equals(ENGINE_TYPE)) {
            throw new IllegalArgumentException("엔진명은 다음과 같아야합니다: " + ENGINE_TYPE);
        }
        if (!createTableRequestDTO.getCharset().equals(CHARSET)) {
            throw new IllegalArgumentException("캐릭터셋은 다음과 같아야합니다: " + CHARSET);
        }
        if (!createTableRequestDTO.getCollate().equals(COLLATE)) {
            throw new IllegalArgumentException("콜레이션은 다음과 같아야합니다: " + COLLATE);
        }
        if (createTableRequestDTO.getTableComment().isBlank() || createTableRequestDTO.getTableComment().isEmpty()) {
            throw new IllegalArgumentException("테이블 코멘트가 존재하지 않습니다.");
        }
    }

    private void checkConstraintType(List<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            if (!CONSTRAINT_TYPE.contains(constraint.getType())) {
                throw new IllegalArgumentException("허용된 Constraint Type 이 아닙니다. [" + constraint.getType() + "]");
            }
        }
    }

    private boolean isSnakeCase(String str) {
        return SNAKE_CASE_PATTERN.matcher(str).matches();
    }

    private boolean isLowerCaseString(String str) {
        return PURE_LOWER_CASE_PATTER.matcher(str).matches();
    }
}
