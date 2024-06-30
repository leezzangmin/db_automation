package zzangmin.db_automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.client.MysqlClient;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.dml.SelectQueryRequestDTO;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DMLService {

    private final MysqlClient mysqlClient;


    public List<Map<String, Object>> select(DatabaseConnectionInfo databaseConnectionInfo, SelectQueryRequestDTO selectQueryRequestDTO) {
        List<Map<String, Object>> result = mysqlClient.executeSelectQuery(databaseConnectionInfo, selectQueryRequestDTO.toSQL());
        return result;
    }
}
