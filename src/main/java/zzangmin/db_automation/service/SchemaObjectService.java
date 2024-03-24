package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.entity.SchemaObject;
import zzangmin.db_automation.entity.SchemaObjectType;
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
public class SchemaObjectService {

    private final SchemaObjectRepository schemaObjectRepository;

    @Transactional
    public void saveTables(String serviceName, String schemaName, List<Table> tables) throws Exception {
        List<SchemaObject> schemaObjects = new ArrayList<>();

        for (Table table : tables) {
            String encryptedJsonTable = makeEncryptedJsonString(table);
            SchemaObject schemaObject = SchemaObject.builder()
                    .schemaObjectType(SchemaObjectType.TABLE)
                    .databaseName(schemaName)
                    .schemaObjectName(table.getTableName())
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonTable)
                    .build();
            schemaObjects.add(schemaObject);
        }

        schemaObjectRepository.saveAll(schemaObjects);
    }

    @Transactional
    public void saveDatabases(String serviceName, Map<String, String> schemaCreateStatements) throws Exception {
        List<SchemaObject> schemaObjects = new ArrayList<>();
        for (String schemaName : schemaCreateStatements.keySet()) {
            String encryptedJsonCreateDatabase = makeEncryptedJsonString(schemaCreateStatements.get(schemaName));
            SchemaObject schemaObject = SchemaObject.builder()
                    .schemaObjectType(SchemaObjectType.DATABASE)
                    .databaseName(schemaName)
                    .schemaObjectName(schemaName)
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonCreateDatabase)
                    .build();
            schemaObjects.add(schemaObject);
        }
        log.info("schemaObjects: {}", schemaObjects);
        schemaObjectRepository.saveAll(schemaObjects);
    }

    @Transactional(readOnly = true)
    public Map<String, String> findDatabases(String serviceName) {
        log.info("serviceName: {}", serviceName);
        return schemaObjectRepository.findByServiceNameAndSchemaObjectType(serviceName, SchemaObjectType.DATABASE)
                .stream()
                .collect(Collectors.toMap(
                        schemaObject -> schemaObject.getDatabaseName(),
                        schemaObject -> {
                            try {
                                return (String) encryptedJsonStringToObject(schemaObject.getEncryptedJsonString());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }));
    }

    @Transactional(readOnly = true)
    public List<Table> findTables(String serviceName, SchemaObjectType schemaObjectType) {
        List<SchemaObject> schemaTables = schemaObjectRepository.findByServiceNameAndSchemaObjectType(serviceName, schemaObjectType);
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
