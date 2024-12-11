package zzangmin.db_automation.dto.response.standardcheck;

import com.slack.api.model.block.LayoutBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class StandardCheckResultResponseDTO {
    private String accountId;
    private String instanceName;
    private StandardType standardType;
    private String standardName;
    // 표준값
    private String standardValue;
    // 설정되어있는값
    private String currentValue;
    private String errorDescription;

    public enum StandardType {
        ACCOUNT,
        CLUSTER_CREATION,
        INSTANCE_CREATION,
        PARAMETER,
        PLUGIN_COMPONENT,
        SCHEMA,
        TAG,
        VARIABLE
    }

}
