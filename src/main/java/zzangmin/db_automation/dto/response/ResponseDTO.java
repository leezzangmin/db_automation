package zzangmin.db_automation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class ResponseDTO {
    private String executeDuration;

    public void setExecuteDuration(long executeDuration) {
        this.executeDuration = millisecondToSecond(executeDuration) + "초";
    }

    private double millisecondToSecond(long millisecond) {
        return (double) millisecond / 1000;
    }
}
