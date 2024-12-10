package zzangmin.db_automation.standardvalue;

public class SecretManagerStandard {

    public static final String DB_CREDENTIAL_POSTFIX = "-db-credential";

    public static String generateStandardSecretManagerName(String serviceNameTagValue, String currentProfile) {
        String secretName = serviceNameTagValue + "-" + currentProfile + DB_CREDENTIAL_POSTFIX;
        return secretName;
    }

}
