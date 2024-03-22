package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.entity.SchemaObject;
import zzangmin.db_automation.entity.SchemaType;
import zzangmin.db_automation.entity.Table;
import zzangmin.db_automation.repository.SchemaObjectRepository;
import zzangmin.db_automation.util.EncryptionUtil;
import zzangmin.db_automation.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchemaService {

    private final SchemaObjectRepository schemaObjectRepository;

    @Transactional
    public void saveTable(String serviceName, Table table) throws Exception {
        String encryptedJsonTable = makeEncryptedJsonString(table);

        SchemaObject schema = SchemaObject.builder()
                .schemaType(SchemaType.TABLE)
                .schemaName(table.getTableName())
                .serviceName(serviceName)
                .encryptedJsonString(encryptedJsonTable)
                .build();

        schemaObjectRepository.save(schema);
    }

    @Transactional
    public void saveDatabases(String serviceName, Map<String, String> schemaCreateStatements) throws Exception {
        List<SchemaObject> schemas = new ArrayList<>();
        for (String schemaName : schemaCreateStatements.keySet()) {
            String encryptedJsonCreateDatabase = makeEncryptedJsonString(schemaCreateStatements.get(schemaName));
            SchemaObject schema = SchemaObject.builder()
                    .schemaType(SchemaType.DATABASE)
                    .schemaName(schemaName)
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonCreateDatabase)
                    .build();
            schemas.add(schema);
        }
        log.info("schemas: {}", schemas);
        schemaObjectRepository.saveAll(schemas);
    }


    @Transactional(readOnly = true)
    public List<Table> findTables(String serviceName, SchemaType schemaType) {
        List<SchemaObject> schemaTables = schemaObjectRepository.findByServiceNameAndSchemaType(serviceName, schemaType);
        List<Table> tables = schemaTables.stream()
                .map(schema -> {
                    try {
                        return (Table) encryptedJsonStringToObject(schema.getEncryptedJsonString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return tables;
    }


    private String makeEncryptedJsonString(Object object) throws Exception {
        String jsonString = JsonUtil.toJson(object);
        String encryptedJsonString = EncryptionUtil.encrypt(jsonString);
        return encryptedJsonString;
    }

    private Object encryptedJsonStringToObject(String encryptedJsonString) throws Exception {
        String jsonString = EncryptionUtil.decrypt(encryptedJsonString);
        Object object = JsonUtil.toObject(jsonString);

        return object;
    }
}
