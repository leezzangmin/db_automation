package zzangmin.db_automation.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Table {
    @NotBlank
    private String tableName;
    @NotBlank
    private List<Column> columns = new ArrayList<>();
    @NotBlank
    private List<Constraint> constraints = new ArrayList<>();
    @NotBlank
    private String tableEngine;
    @NotBlank
    private String tableCharset;
    @NotBlank
    private String tableCollate;
    @NotBlank
    private String tableComment;

    public void addColumns(List<Column> columns) {
        this.columns.addAll(columns);
    }

    public void addConstraints(List<Constraint> constraints) {
        this.constraints.addAll(constraints);
    }
}
