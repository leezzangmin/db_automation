package zzangmin.db_automation.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.config.SlackConfig;

@Slf4j
@RequiredArgsConstructor
@Component
public class SlackRequestSignatureVerifier {

    private final SlackConfig slackConfig;

    public boolean validateRequest(String slackSignature, String timestamp, String requestBody) {
        try {
            String secret = slackConfig.slackAppSigningSecret;
            String baseString = "v0:" + timestamp + ":" + requestBody;

            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(baseString.getBytes());
            String mySignature = "v0=" + Hex.encodeHexString(hash);

            log.info("mySignature: {}", mySignature);
            log.info("slackSignature: {}", slackSignature);
            return mySignature.equals(slackSignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
