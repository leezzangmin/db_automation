package zzangmin.db_automation.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static String METADATA_ENCRYPT_KEY;

    @Value("${METADATA_ENCRYPT_KEY}")
    public void setMetadataEncryptKey(String key) {
        EncryptionUtil.METADATA_ENCRYPT_KEY = key;
    }

    private static final String ALGORITHM = "AES";


    public static String encrypt(String value) throws Exception {
        if (METADATA_ENCRYPT_KEY == null) {
            throw new IllegalStateException("Encryption key is not initialized. Check METADATA_ENCRYPT_KEY environment variable.");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(METADATA_ENCRYPT_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedValue = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encryptedValue);
    }

    public static String decrypt(String encryptedValue) throws Exception {
        if (METADATA_ENCRYPT_KEY == null) {
            throw new IllegalStateException("Encryption key is not initialized. Check METADATA_ENCRYPT_KEY environment variable.");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(METADATA_ENCRYPT_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);
        byte[] decValue = cipher.doFinal(decodedValue);
        return new String(decValue);
    }
}
