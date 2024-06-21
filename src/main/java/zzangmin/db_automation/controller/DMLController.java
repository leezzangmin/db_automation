package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.dml.DMLRequestDTO;
import zzangmin.db_automation.dto.request.dml.SelectQueryRequestDTO;
import zzangmin.db_automation.dto.response.dml.SelectQueryResponseDTO;
import zzangmin.db_automation.service.DMLService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DMLController {
    private final DMLService dmlService;

    @GetMapping("/dml/validate")
    public String validate(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                               @RequestBody DMLRequestDTO dmlRequestDTO) {
        // todo: validation
        return "ok";
    }

    @GetMapping("/dml/select")
    public SelectQueryResponseDTO select(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                         @RequestBody SelectQueryRequestDTO dmlRequestDTO,
                                         String slackUserId) {
        String resultJson = dmlService.select(databaseConnectionInfo, dmlRequestDTO);

        return new SelectQueryResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                dmlRequestDTO.getSchemaName(),
                resultJson);
    }
}
