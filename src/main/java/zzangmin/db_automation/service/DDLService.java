package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.repository.JdbcRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class DDLService {

    private final JdbcRepository jdbcRepository;

    public void validate(String dbName, String ddlCommand) {

    }

}
