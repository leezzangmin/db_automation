package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zzangmin.db_automation.dto.CreateTableRequestDTO;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.repository.JdbcRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@RequiredArgsConstructor
@Service
public class DDLService {

    private final JdbcRepository jdbcRepository;

    public void validate(String dbName, String ddlCommand) {

    }

    public void createTable(DatabaseConnectionInfo databaseConnectionInfo, CreateTableRequestDTO createTableRequestDTO) {

        StringBuilder result = new StringBuilder();

        try {
            Connection connection = DriverManager.getConnection(
                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ddlCommand);

            while (resultSet.next()) {
                result.append(resultSet.getString(1));
            }

            result.append("DDL executed successfully on database: ").append(databaseConnectionInfo.getDatabaseName());

            statement.close();
            connection.close();
        } catch (Exception e) {
            result.append(e);
            result.append("Failed to execute DDL on database: ").append(databaseConnectionInfo.getDatabaseName());
            result.append("\n").append(e.getMessage());
        }
    }

}
