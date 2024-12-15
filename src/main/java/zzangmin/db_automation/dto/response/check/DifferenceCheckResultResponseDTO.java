package zzangmin.db_automation.dto.response.check;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class DifferenceCheckResultResponseDTO {
    private String sourceInstanceName;
    private String otherInstanceName;
    private String sourceEnvironment;
    private String otherEnvironment;
    private String differenceDescription;
}
