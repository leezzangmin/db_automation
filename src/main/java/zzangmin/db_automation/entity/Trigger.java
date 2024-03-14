package zzangmin.db_automation.entity;

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