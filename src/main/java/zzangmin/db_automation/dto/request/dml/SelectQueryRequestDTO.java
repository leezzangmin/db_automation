package zzangmin.db_automation.dto.request.dml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zzangmin.db_automation.entity.DatabaseRequestCommandGroup;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectQueryRequestDTO implements DMLRequestDTO {

    private String schemaName;

    private List<String> selectItems;
    private String fromItem;
    private List<String> joins;
    private String where;
    private List<String> orderBy;

    @Override
    public String toString() {
        return "SelectQuery{" +
                "selectItems=" + selectItems +
                ", fromItem='" + fromItem + '\'' +
                ", joins=" + joins +
                ", where='" + where + '\'' +
                ", orderBy=" + orderBy +
                '}';
    }

    public String toSQL() {
        StringBuilder sql = new StringBuilder();

        // SELECT
        sql.append("SELECT ");
        StringJoiner selectJoiner = new StringJoiner(", ");
        for (String item : selectItems) {
            selectJoiner.add(item);
        }
        sql.append(selectJoiner.toString());

        // FROM
        sql.append(" FROM ").append(fromItem);

        // JOINs
        if (joins != null && !joins.isEmpty()) {
            for (String join : joins) {
                sql.append(" ").append(join);
            }
        }

        // WHERE
        if (where != null && !where.isEmpty()) {
            sql.append(" WHERE ").append(where);
        }

        // ORDER BY
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ");
            StringJoiner orderByJoiner = new StringJoiner(", ");
            for (String order : orderBy) {
                orderByJoiner.add(order);
            }
            sql.append(orderByJoiner.toString());
        }

        return sql.toString();

    @Override
    public DatabaseRequestCommandGroup.CommandType extractCommandType() {
        return DatabaseRequestCommandGroup.CommandType.SELECT;
    }

    @Override
    public String toSQL() {
        return this.SQL;
    }

}
