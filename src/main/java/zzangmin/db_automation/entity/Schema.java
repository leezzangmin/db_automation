package zzangmin.db_automation.entity;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Schema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private SchemaType schemaType;
    private String schemaName;
    private String serviceName;
    private String encryptedJsonString;

}
