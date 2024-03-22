package zzangmin.db_automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zzangmin.db_automation.entity.Schema;
import zzangmin.db_automation.entity.SchemaType;

import java.util.List;

@Repository
public interface SchemaRepository extends JpaRepository<Schema, Long> {

    @Query
    List<Schema> findByServiceNameAndSchemaType(String serviceName, SchemaType schemaType);
}
