package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.mysqlobject.SchemaObject;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemaObjectRepository extends JpaRepository<SchemaObject, Long> {

    @Query
    List<SchemaObject> findByServiceNameAndDatabaseNameAndSchemaObjectType(String serviceName, String databaseName, SchemaObject.SchemaObjectType schemaObjectType);

    @Query
    Optional<SchemaObject> findByServiceNameAndDatabaseNameAndSchemaObjectTypeAndSchemaObjectName(String serviceName, String databaseName, SchemaObject.SchemaObjectType schemaObjectType, String schemaObjectName);

    @Query
    List<SchemaObject> findByServiceNameAndSchemaObjectType(String serviceName, SchemaObject.SchemaObjectType schemaObjectType);
}
