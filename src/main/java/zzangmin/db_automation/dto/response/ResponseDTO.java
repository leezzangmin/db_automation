package zzangmin.db_automation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class ResponseDTO {
    private double executeDuration;

    public void setExecuteDuration(double executeDuration) {
        this.executeDuration = executeDuration;
    }
}
