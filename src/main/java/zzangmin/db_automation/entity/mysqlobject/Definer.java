package zzangmin.db_automation.entity.mysqlobject;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Definer {

    @NotBlank
    private String userName;
    @NotBlank
    private String ip;

    public boolean isEqualDefinerName(Definer otherDefiner) {
        return this.userName.equals(otherDefiner.userName);
    }


    // definerString = username@
    public static Definer splitDefiner(String definerString) {
        definerString.replace("'","").replace("`","");
        String[] splitDefinerString = definerString.split("@");
        return new Definer(splitDefinerString[0], splitDefinerString[1]);
    }
}
