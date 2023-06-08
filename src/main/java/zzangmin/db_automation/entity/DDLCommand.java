package zzangmin.db_automation.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class DDLCommand {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CommandType CommandType;
    private String schemaName;
    private String tableName;
//    private List<Column> columns = new ArrayList<>();
//    private List<Constraint> constraints = new ArrayList<>();
    private String engine;
    private String charset;
    private String collate;
    private String tableComment;

    public String toSQL() {
        return "todo";

    }
}
