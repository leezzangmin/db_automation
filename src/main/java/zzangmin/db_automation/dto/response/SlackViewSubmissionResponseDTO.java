package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public class SlackViewSubmissionResponseDTO {
    private String response_action;
    private Map<String, String> errors = new HashMap<>();

}
