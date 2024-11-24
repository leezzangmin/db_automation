package zzangmin.db_automation.entity.mysqlobject;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SchemaObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private SchemaObjectType schemaObjectType;
    private String databaseName; // ex) aipers_hr
    private String schemaObjectName;
    private String serviceName;
    private String encryptedJsonString;

    public void update(SchemaObject newSchemaObject) {
        this.encryptedJsonString = newSchemaObject.getEncryptedJsonString();
    }

    public enum SchemaObjectType {
        TABLE,
        FUNCTION,
        PROCEDURE,
        TRIGGER,
        VIEW,
        DATABASE,
        ACCOUNT
    }

}
