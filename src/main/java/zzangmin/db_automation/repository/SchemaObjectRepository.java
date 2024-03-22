package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.SchemaObject;
import zzangmin.db_automation.entity.SchemaType;

import java.util.List;

@Repository
public interface SchemaObjectRepository extends JpaRepository<SchemaObject, Long> {

    @Query
    List<SchemaObject> findByServiceNameAndSchemaType(String serviceName, SchemaType schemaType);
}
