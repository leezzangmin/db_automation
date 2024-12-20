package zzangmin.db_automation.entity.mysqlobject;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import zzangmin.db_automation.util.ProfileUtil;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Function {

    @NotBlank
    private String functionName;
    private String dataType;
    private String characterSetName;
    private String collationName;
    private String routineDefinition;
    private Boolean isDeterministic;
    private Definer definer;
    private String characterSetClient;
    private String collationConnection;
    private String databaseCollation;
    private String securityType;

    public String reportDifference(Function otherFunction) {
        StringBuilder difference = new StringBuilder();

        if (otherFunction == null) {
            difference.append(String.format("`%s` function을 %s에서 찾을 수 없습니다.", this.functionName, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
        }

        if (this.functionName != null && !this.functionName.equals(otherFunction.functionName)) {
            difference.append(String.format("function 이름이 다릅니다: `%s` <-> `%s`\n", this.functionName, otherFunction.functionName));
        }
        if (this.dataType != null && !this.dataType.equals(otherFunction.dataType)) {
            difference.append(String.format("`%s`: function dataType 이 다릅니다: `%s` <-> `%s`\n", this.functionName, this.dataType, otherFunction.dataType));
        }
        if (this.characterSetName != null && !this.characterSetName.equals(otherFunction.characterSetName)) {
            difference.append(String.format("`%s`: function characterSetName 이 다릅니다: `%s` <-> %s\n", this.functionName, this.characterSetName, otherFunction.characterSetName));
        }
        if (this.collationName != null && !this.collationName.equals(otherFunction.collationName)) {
            difference.append(String.format("`%s`: function collationName 이 다릅니다: %s <-> %s\n", this.functionName, this.collationName, otherFunction.collationName));
        }
        if (this.routineDefinition != null && !this.routineDefinition.equals(otherFunction.routineDefinition)) {
            difference.append(String.format("`%s`: function routineDefinition 이 다릅니다\n", this.functionName));
        }
        if (!this.isDeterministic == otherFunction.isDeterministic) {
            difference.append(String.format("%s: function isDeterministic 이 다릅니다: %s <-> %s\n", this.functionName, this.isDeterministic, otherFunction.isDeterministic));
        }
        if (this.definer != null && !this.definer.isEqualDefinerName(otherFunction.definer)) {
            difference.append(String.format("%s: function definerName 이 다릅니다: %s <-> %s\n", this.functionName, this.definer.getUserName(), otherFunction.getDefiner().getUserName()));
        }
        if (this.characterSetClient != null && !this.characterSetClient.equals(otherFunction.characterSetClient)) {
            difference.append(String.format("%s: function characterSetClient 이 다릅니다: %s <-> %s\n", this.functionName, this.characterSetClient, otherFunction.characterSetClient));
        }
        if (this.collationConnection != null && !this.collationConnection.equals(otherFunction.collationConnection)) {
            difference.append(String.format("%s: function collationConnection 이 다릅니다: %s <-> %s%n", this.functionName, this.collationConnection, otherFunction.collationConnection));
        }
        if (this.databaseCollation != null && !this.databaseCollation.equals(otherFunction.databaseCollation)) {
            difference.append(String.format("%s: function databaseCollation 이 다릅니다: %s <-> %s\n", this.functionName, this.databaseCollation, otherFunction.databaseCollation));
        }
        if (this.securityType != null && !this.securityType.equals(otherFunction.securityType)) {
            difference.append(String.format("%s: function securityType 이 다릅니다: %s <-> %s\n", this.functionName, this.securityType, otherFunction.securityType));
        }
        return difference.toString();
    }

}

/**
 * mysql> SELECT * FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE = 'FUNCTION' and routine_schema='sample_schema' limit 1\G
 * *************************** 1. row ***************************
 *            SPECIFIC_NAME: f1
 *          ROUTINE_CATALOG: def
 *           ROUTINE_SCHEMA: sample_schema
 *             ROUTINE_NAME: f1
 *             ROUTINE_TYPE: FUNCTION
 *                DATA_TYPE: decimal
 * CHARACTER_MAXIMUM_LENGTH: NULL
 *   CHARACTER_OCTET_LENGTH: NULL
 *        NUMERIC_PRECISION: 10
 *            NUMERIC_SCALE: 2
 *       DATETIME_PRECISION: NULL
 *       CHARACTER_SET_NAME: NULL
 *           COLLATION_NAME: NULL
 *           DTD_IDENTIFIER: decimal(10,2)
 *             ROUTINE_BODY: SQL
 *       ROUTINE_DEFINITION: BEGIN
 *     RETURN p_price * 1.11;
 * END
 *            EXTERNAL_NAME: NULL
 *        EXTERNAL_LANGUAGE: SQL
 *          PARAMETER_STYLE: SQL
 *         IS_DETERMINISTIC: NO
 *          SQL_DATA_ACCESS: CONTAINS SQL
 *                 SQL_PATH: NULL
 *            SECURITY_TYPE: DEFINER
 *                  CREATED: 2024-03-13 11:53:10
 *             LAST_ALTERED: 2024-03-13 11:53:10
 *                 SQL_MODE:
 *          ROUTINE_COMMENT:
 *                  DEFINER: admin@%
 *     CHARACTER_SET_CLIENT: euckr
 *     COLLATION_CONNECTION: euckr_korean_ci
 *       DATABASE_COLLATION: utf8mb4_0900_ai_ci
 */