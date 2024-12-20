package zzangmin.db_automation.entity.mysqlobject;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import zzangmin.db_automation.util.ProfileUtil;

@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedure {
    @NotBlank
    private String procedureName;
    private String dataType;
    private String characterSetName;
    private String collationName;
    private String routineDefinition;
    private Boolean isDeterministic;
    private Definer definer;
    private String characterSetClient;
    private String collationConnection;
    private String databaseCollation;
    private String securityType;

    public String reportDifference(Procedure otherProcedure) {
        StringBuilder difference = new StringBuilder();

        if (otherProcedure == null) {
            difference.append(String.format("`%s` procedure을 %s에서 찾을 수 없습니다.", this.procedureName, ProfileUtil.CURRENT_ENVIRONMENT_PROFILE));
        }

        if (this.procedureName != null && !this.procedureName.equals(otherProcedure.procedureName)) {
            difference.append(String.format("procedure 이름이 다릅니다: `%s` <-> `%s`\n", this.procedureName, otherProcedure.procedureName));
        }
        if (this.dataType != null && !this.dataType.equals(otherProcedure.dataType)) {
            difference.append(String.format("`%s`: procedure dataType 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.dataType, otherProcedure.dataType));
        }
        if (this.characterSetName != null && !this.characterSetName.equals(otherProcedure.characterSetName)) {
            difference.append(String.format("`%s`: procedure characterSetName 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.characterSetName, otherProcedure.characterSetName));
        }
        if (this.collationName != null && !this.collationName.equals(otherProcedure.collationName)) {
            difference.append(String.format("`%s`: procedure collationName 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.collationName, otherProcedure.collationName));
        }
        if (this.routineDefinition != null && !this.routineDefinition.equals(otherProcedure.routineDefinition)) {
            difference.append(String.format("`%s`: procedure routineDefinition 이 다릅니다: \n", this.procedureName));
        }
        if (!this.isDeterministic == otherProcedure.isDeterministic) {
            difference.append(String.format("`%s`: procedure isDeterministic 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.isDeterministic, otherProcedure.isDeterministic));
        }
        if (this.definer != null && !this.definer.isEqualDefinerName(otherProcedure.definer)) {
            difference.append(String.format("`%s`: procedure definerName 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.definer.getUserName(), otherProcedure.getDefiner().getUserName()));
        }
        if (this.characterSetClient != null && !this.characterSetClient.equals(otherProcedure.characterSetClient)) {
            difference.append(String.format("`%s`: procedure characterSetClient 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.characterSetClient, otherProcedure.characterSetClient));
        }
        if (this.collationConnection != null && !this.collationConnection.equals(otherProcedure.collationConnection)) {
            difference.append(String.format("`%s`: procedure collationConnection 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.collationConnection, otherProcedure.collationConnection));
        }
        if (this.databaseCollation != null && !this.databaseCollation.equals(otherProcedure.databaseCollation)) {
            difference.append(String.format("`%s`: procedure databaseCollation 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.databaseCollation, otherProcedure.databaseCollation));
        }
        if (this.securityType != null && !this.securityType.equals(otherProcedure.securityType)) {
            difference.append(String.format("`%s`: procedure securityType 이 다릅니다: `%s` <-> `%s`\n", this.procedureName, this.securityType, otherProcedure.securityType));
        }

        return difference.toString();
    }
}
