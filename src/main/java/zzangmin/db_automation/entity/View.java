package zzangmin.db_automation.entity;

import lombok.*;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class View {

    private String viewName;
    private String viewDefinition;
    private Definer definer;
    private String securityType;
    private String characterSetClient;
    private String collationConnection;

    public String reportDifference(View otherTrigger) {
        StringBuilder difference = new StringBuilder();
        StringBuilder result = new StringBuilder();

        if (otherTrigger == null) {
            difference.append(String.format("%s view 를 stage에서 찾을 수 없습니다.\n", this.viewName));
            return difference.toString();
        }
        if (this.viewName != null && !this.viewName.equals(otherTrigger.viewName)) {
            difference.append(String.format("view 이름이 다릅니다: %s <-> %s\n", this.viewName, otherTrigger.viewName));
        }
        if (this.viewDefinition != null && !this.viewDefinition.equals(otherTrigger.viewDefinition)) {
            difference.append(String.format("%s: view viewDefinition 이 다릅니다: \n%s \n\n%s\n", this.viewName, this.viewDefinition, otherTrigger.viewDefinition));
        }
        if (this.securityType != null && !this.securityType.equals(otherTrigger.securityType)) {
            difference.append(String.format("%s: view securityType 이 다릅니다: %s <-> %s\n", this.viewName, this.securityType, otherTrigger.securityType));
        }
        if (this.definer != null && !this.definer.isEqualDefinerName(otherTrigger.definer)) {
            difference.append(String.format("%s: view definerName 이 다릅니다: %s <-> %s\n", this.viewName, this.definer.getUserName(), otherTrigger.getDefiner().getUserName()));
        }
        if (this.characterSetClient != null && !this.characterSetClient.equals(otherTrigger.characterSetClient)) {
            difference.append(String.format("%s: view characterSetClient 이 다릅니다: %s <-> %s\n", this.viewName, this.characterSetClient, otherTrigger.characterSetClient));
        }
        if (this.collationConnection != null && !this.collationConnection.equals(otherTrigger.collationConnection)) {
            difference.append(String.format("%s: view collationConnection 이 다릅니다: %s <-> %s\n", this.viewName, this.collationConnection, otherTrigger.collationConnection));
        }
        if (difference.toString().isBlank()) {
            return difference.toString();
        }

        result.append(String.format("\nVIEW [%s] 검사 결과: \n", this.getViewName()));
        result.append(difference);
        result.append("\n");
        return result.toString();
    }

}

/**
 * mysql> select * from information_schema.views where table_schema like '%sample%' limit 1\G
 * *************************** 1. row ***************************
 *        TABLE_CATALOG: def
 *         TABLE_SCHEMA: sample_schema
 *           TABLE_NAME: CustomerOrderCount
 *      VIEW_DEFINITION: select `sample_schema`.`orders`.`customer_id` AS `customer_id`,count(0) AS `total_orders` from `sample_schema`.`orders` group by `sample_schema`.`orders`.`customer_id`
 *         CHECK_OPTION: NONE
 *         IS_UPDATABLE: NO
 *              DEFINER: admin@%
 *        SECURITY_TYPE: DEFINER
 * CHARACTER_SET_CLIENT: euckr
 * COLLATION_CONNECTION: euckr_korean_ci
 */