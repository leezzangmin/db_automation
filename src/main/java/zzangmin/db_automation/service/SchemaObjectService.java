package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.entity.*;
import zzangmin.db_automation.repository.SchemaObjectRepository;
import zzangmin.db_automation.util.EncryptionUtil;
import zzangmin.db_automation.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            log.info("table: {}", table);
        }
        log.info("table schemaObjects: {}", schemaObjects);
        upsert(schemaObjects);
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
        log.info("database schemaObjects: {}", schemaObjects);
        upsert(schemaObjects);
    }

    @Transactional
    public void saveViews(String serviceName, String schemaName, List<View> views) throws Exception {
        List<SchemaObject> schemaObjects = new ArrayList<>();

        for (View view : views) {
            String encryptedJsonView = makeEncryptedJsonString(view);
            SchemaObject schemaObject = SchemaObject.builder()
                    .schemaObjectType(SchemaObjectType.VIEW)
                    .databaseName(schemaName)
                    .schemaObjectName(view.getViewName())
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonView)
                    .build();
            schemaObjects.add(schemaObject);
        }
        log.info("view schemaObjects: {}", schemaObjects);
        upsert(schemaObjects);
    }

    @Transactional
    public void saveFunctions(String serviceName, String schemaName, List<Function> functions) throws Exception {
        List<SchemaObject> schemaObjects = new ArrayList<>();

        for (Function function : functions) {
            String encryptedJsonView = makeEncryptedJsonString(function);
            SchemaObject schemaObject = SchemaObject.builder()
                    .schemaObjectType(SchemaObjectType.FUNCTION)
                    .databaseName(schemaName)
                    .schemaObjectName(function.getFunctionName())
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonView)
                    .build();
            schemaObjects.add(schemaObject);
        }
        log.info("function schemaObjects: {}", schemaObjects);
        upsert(schemaObjects);
    }

    @Transactional
    public void saveTriggers(String serviceName, String schemaName, List<Trigger> triggers) throws Exception {
        List<SchemaObject> schemaObjects = new ArrayList<>();

        for (Trigger trigger : triggers) {
            String encryptedJsonView = makeEncryptedJsonString(trigger);
            SchemaObject schemaObject = SchemaObject.builder()
                    .schemaObjectType(SchemaObjectType.TRIGGER)
                    .databaseName(schemaName)
                    .schemaObjectName(trigger.getTriggerName())
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonView)
                    .build();
            schemaObjects.add(schemaObject);
        }
        log.info("trigger schemaObjects: {}", schemaObjects);
        upsert(schemaObjects);
    }


    @Transactional
    public void saveProcedures(String serviceName, String schemaName, List<Procedure> procedures) throws Exception {
        List<SchemaObject> schemaObjects = new ArrayList<>();

        for (Procedure procedure : procedures) {
            String encryptedJsonView = makeEncryptedJsonString(procedure);
            SchemaObject schemaObject = SchemaObject.builder()
                    .schemaObjectType(SchemaObjectType.PROCEDURE)
                    .databaseName(schemaName)
                    .schemaObjectName(procedure.getProcedureName())
                    .serviceName(serviceName)
                    .encryptedJsonString(encryptedJsonView)
                    .build();
            schemaObjects.add(schemaObject);
        }
        log.info("procedure schemaObjects: {}", schemaObjects);
        upsert(schemaObjects);
    }

    @Transactional(readOnly = true)
    public List<Procedure> findProcedures(String serviceName, String schemaName) {
        List<SchemaObject> schemaProcedures = schemaObjectRepository.findByServiceNameAndDatabaseNameAndSchemaObjectType(serviceName, schemaName, SchemaObjectType.PROCEDURE);
        List<Procedure> procedures = schemaProcedures.stream()
                .map(schema -> {
                    try {
                        return encryptedJsonStringToObject(schema.getEncryptedJsonString(), Procedure.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return procedures;
    }

    @Transactional(readOnly = true)
    public List<Trigger> findTriggers(String serviceName, String schemaName) {
        List<SchemaObject> schemaTriggers = schemaObjectRepository.findByServiceNameAndDatabaseNameAndSchemaObjectType(serviceName, schemaName,
                SchemaObjectType.TRIGGER);
        List<Trigger> triggers = schemaTriggers.stream()
                .map(schema -> {
                    try {
                        return encryptedJsonStringToObject(schema.getEncryptedJsonString(), Trigger.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return triggers;
    }

    @Transactional(readOnly = true)
    public List<Function> findFunctions(String serviceName, String schemaName) {
        List<SchemaObject> schemaFunctions = schemaObjectRepository.findByServiceNameAndDatabaseNameAndSchemaObjectType(serviceName, schemaName,
                SchemaObjectType.FUNCTION);
        List<Function> functions = schemaFunctions.stream()
                .map(schema -> {
                    try {
                        return encryptedJsonStringToObject(schema.getEncryptedJsonString(), Function.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return functions;
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
                                return encryptedJsonStringToObject(schemaObject.getEncryptedJsonString(), String.class);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }));
    }

    @Transactional(readOnly = true)
    public List<Table> findTables(String serviceName, String schemaName) {
        List<SchemaObject> schemaTables = schemaObjectRepository.findByServiceNameAndDatabaseNameAndSchemaObjectType(serviceName, schemaName, SchemaObjectType.TABLE);
        List<Table> tables = schemaTables.stream()
                .map(schema -> {
                    try {
                        return encryptedJsonStringToObject(schema.getEncryptedJsonString(), Table.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return tables;
    }

    @Transactional(readOnly = true)
    public List<View> findViews(String serviceName, String schemaName) {
        List<SchemaObject> schemaViews = schemaObjectRepository.findByServiceNameAndDatabaseNameAndSchemaObjectType(serviceName, schemaName, SchemaObjectType.VIEW);
        List<View> views = schemaViews.stream()
                .map(schema -> {
                    try {
                        return encryptedJsonStringToObject(schema.getEncryptedJsonString(), View.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return views;
    }


    // 이미 값이 있으면 update(더티체킹), 없으면 insert
    private void upsert(List<SchemaObject> schemaObjects) {
        for (SchemaObject schemaObject : schemaObjects) {
            Optional<SchemaObject> findSchemaObject = schemaObjectRepository.findByServiceNameAndDatabaseNameAndSchemaObjectTypeAndSchemaObjectName(schemaObject.getServiceName(),
                    schemaObject.getDatabaseName(),
                    schemaObject.getSchemaObjectType(),
                    schemaObject.getSchemaObjectName());
            if (findSchemaObject.isPresent()) {
                findSchemaObject.get().update(schemaObject);
                continue;
            }
            schemaObjectRepository.save(schemaObject);
        }
    }

    private String makeEncryptedJsonString(Object object) throws Exception {
        String jsonString = JsonUtil.toJson(object);
        String encryptedJsonString = EncryptionUtil.encrypt(jsonString);
        return encryptedJsonString;
    }

    private <T> T encryptedJsonStringToObject(String encryptedJsonString, Class<T> valueType) throws Exception {
        String jsonString = EncryptionUtil.decrypt(encryptedJsonString);
        return JsonUtil.toObject(jsonString, valueType);

    }
}
