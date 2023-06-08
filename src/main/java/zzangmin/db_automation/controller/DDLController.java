package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.*;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.service.DDLService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DDLController {

    private final DDLService ddlService;

//    @PostMapping("/ddl/validate")
//    public String validCommand(@RequestParam String dbName, @RequestParam String ddlCommand) {
//        return "ok";
//
//    }

    @PutMapping("/ddl/column")
    public String addColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody AddColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PatchMapping("/ddl/column")
    public String alterColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody AlterColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PutMapping("/ddl/index")
    public String createIndex(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody CreateIndexRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PutMapping("/ddl/table")
    public String createTable(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody CreateTableRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @DeleteMapping("/ddl/column")
    public String deleteColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody DeleteColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }

    @PatchMapping("/ddl/varchar")
    public String extendVarcharColumn(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody ExtendVarcharColumnRequestDTO ddlRequestDTO) {
        return "ok";
    }


    @PostMapping("/ddl/execute")
    public String executeCommand(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo, @RequestBody CreateTableRequestDTO ddlRequestDTO) {
        System.out.println("ddlRequestDTO = " + ddlRequestDTO);
        return "ok";

//        StringBuilder result = new StringBuilder();
//
//        try {
//            Connection connection = DriverManager.getConnection(
//                    databaseConnectionInfo.getUrl(), databaseConnectionInfo.getUsername(),"mysql5128*");
//
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(ddlCommand);
//
//            while (resultSet.next()) {
//                result.append(resultSet.getString(1));
//            }
//
//            result.append("DDL executed successfully on database: ").append(databaseConnectionInfo.getDatabaseName());
//
//            statement.close();
//            connection.close();
//        } catch (Exception e) {
//            result.append(e);
//            result.append("Failed to execute DDL on database: ").append(databaseConnectionInfo.getDatabaseName());
//            result.append("\n").append(e.getMessage());
//        }
//        return result.toString();
    }
}

