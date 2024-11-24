package zzangmin.db_automation.entity.mysqlobject;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trigger {
    @NotBlank
    private String triggerName;
    private String eventManipulation;
    private String eventObjectTable;
    private int actionOrder;
    private String actionStatement;
    private String actionOrientation;
    private Definer definer;
    private String characterSetClient;
    private String collationConnection;
    private String databaseCollation;

    public String reportDifference(Trigger otherTrigger) {
        StringBuilder difference = new StringBuilder();

        if (otherTrigger == null) {
            difference.append(String.format("`%s` trigger를 찾을 수 없습니다.", this.triggerName));
        }

        if (this.triggerName != null && !this.triggerName.equals(otherTrigger.triggerName)) {
            difference.append(String.format("trigger 이름이 다릅니다: `%s` <-> `%s`\n", this.triggerName, otherTrigger.triggerName));
        }
        if (this.eventManipulation != null && !this.eventManipulation.equals(otherTrigger.eventManipulation)) {
            difference.append(String.format("`%s`: trigger eventManipulation 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.eventManipulation, otherTrigger.eventManipulation));
        }
        if (this.eventObjectTable != null && !this.eventObjectTable.equals(otherTrigger.eventObjectTable)) {
            difference.append(String.format("`%s`: trigger eventObjectTable 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.eventObjectTable, otherTrigger.eventObjectTable));
        }
        if (this.actionOrder != otherTrigger.actionOrder) {
            difference.append(String.format("`%s`: trigger actionOrder 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.actionOrder, otherTrigger.actionOrder));
        }
        if (this.actionStatement != null && !this.actionStatement.equals(otherTrigger.actionStatement)) {
            difference.append(String.format("`%s`: trigger actionStatement 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.actionStatement, otherTrigger.actionStatement));
        }
        if (this.actionOrientation != null && !this.actionOrientation.equals(otherTrigger.actionOrientation)) {
            difference.append(String.format("`%s`: trigger actionOrientation 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.actionOrientation, otherTrigger.actionOrientation));
        }
        if (this.definer != null && !this.definer.isEqualDefinerName(otherTrigger.definer)) {
            difference.append(String.format("`%s`: trigger definerName 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.definer.getUserName(), otherTrigger.getDefiner().getUserName()));
        }
        if (this.characterSetClient != null && !this.characterSetClient.equals(otherTrigger.characterSetClient)) {
            difference.append(String.format("`%s`: trigger characterSetClient 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.characterSetClient, otherTrigger.characterSetClient));
        }
        if (this.collationConnection != null && !this.collationConnection.equals(otherTrigger.collationConnection)) {
            difference.append(String.format("`%s`: trigger collationConnection 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.collationConnection, otherTrigger.collationConnection));
        }
        if (this.databaseCollation != null && !this.databaseCollation.equals(otherTrigger.databaseCollation)) {
            difference.append(String.format("`%s`: trigger databaseCollation 이 다릅니다: `%s` <-> `%s`\n", this.triggerName, this.databaseCollation, otherTrigger.databaseCollation));
        }
        return difference.toString();
    }

}

/**
 * mysql> select * from information_schema.triggers where trigger_schema like '%schema%'limit 1\G
 * *************************** 1. row ***************************
 *            TRIGGER_CATALOG: def
 *             TRIGGER_SCHEMA: sample_schema
 *               TRIGGER_NAME: AfterOrderInsert
 *         EVENT_MANIPULATION: INSERT
 *       EVENT_OBJECT_CATALOG: def
 *        EVENT_OBJECT_SCHEMA: sample_schema
 *         EVENT_OBJECT_TABLE: orders
 *               ACTION_ORDER: 1
 *           ACTION_CONDITION: NULL
 *           ACTION_STATEMENT: BEGIN
 *     UPDATE products SET stock_quantity = stock_quantity - 1 WHERE product_id = 1;
 * END
 *         ACTION_ORIENTATION: ROW
 *              ACTION_TIMING: AFTER
 * ACTION_REFERENCE_OLD_TABLE: NULL
 * ACTION_REFERENCE_NEW_TABLE: NULL
 *   ACTION_REFERENCE_OLD_ROW: OLD
 *   ACTION_REFERENCE_NEW_ROW: NEW
 *                    CREATED: 2024-03-13 11:53:44.97
 *                   SQL_MODE: NO_ENGINE_SUBSTITUTION
 *                    DEFINER: admin@%
 *       CHARACTER_SET_CLIENT: euckr
 *       COLLATION_CONNECTION: euckr_korean_ci
 *         DATABASE_COLLATION: utf8mb4_0900_ai_ci
 * 1 row in set (0.02 sec)
 */