package zzangmin.db_automation.util;

public class StringMessageUtil {

    public static String convertCreateDatabaseDifferenceMessage(String schemaName, String prodStatement, String stageStatement) {
        StringBuilder differenceResult = new StringBuilder();
        differenceResult.append(schemaName);
        differenceResult.append("의 데이터베이스 생성문이 다릅니다.\nprod: ");
        differenceResult.append(prodStatement);
        differenceResult.append("\nstage: ");
        differenceResult.append(stageStatement);
        differenceResult.append("\n");
        return differenceResult.toString();
    }

}
