package zzangmin.db_automation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.argumentresolver.TargetDatabase;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.dto.request.dml.DMLRequestDTO;
import zzangmin.db_automation.dto.request.dml.SelectQueryRequestDTO;
import zzangmin.db_automation.dto.response.dml.SelectQueryResponseDTO;
import zzangmin.db_automation.service.DMLService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DMLController {
    private final DMLService dmlService;
    private final ObjectMapper objectMapper;

    @GetMapping("/dml/validate")
    public void validate(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                               @RequestBody DMLRequestDTO dmlRequestDTO) {
        if (dmlRequestDTO instanceof SelectQueryRequestDTO) {
            validateSelect(((SelectQueryRequestDTO) dmlRequestDTO).getSql());
            return;
        }
        throw new IllegalArgumentException("validation 미구현 requestDto");
    }

    @GetMapping("/dml/select")
    public SelectQueryResponseDTO select(@TargetDatabase DatabaseConnectionInfo databaseConnectionInfo,
                                         @RequestBody SelectQueryRequestDTO dmlRequestDTO,
                                         String slackUserId) {
        validateSelect(dmlRequestDTO.getSql());
        List<Map<String, Object>> result = dmlService.select(databaseConnectionInfo, dmlRequestDTO);
        String resultJson;
        try {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            resultJson = objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error converting result to JSON", e);
        }

        return new SelectQueryResponseDTO(slackUserId,
                databaseConnectionInfo.getDatabaseName(),
                dmlRequestDTO.getSchemaName(),
                result.toArray().length,
                resultJson);
    }


    private void validateSelect(String selectSQL) {
        try {
            Statement statement = CCJSqlParserUtil.parse(selectSQL);
            if (statement instanceof Select) {
                return;
            } else {
                throw new IllegalArgumentException("Select 가 아닙니다 !");
            }
        } catch (JSQLParserException e) {
            throw new IllegalArgumentException("Invalid SQL !");
        }
    }

}
