package zzangmin.db_automation.config;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class SlackConfig {

    @Value("${slack.token}")
    public String slackToken;
    public static String slackAppSigningSecret;

    public static String DEFAULT_CHANNEL_ID = "C06R9FPERDE"; // TODO: env

    public static final int MAX_MESSAGE_SIZE = 10000;

    @Autowired
    public void setSlackAppSigningSecret(@Value("${slack.app-signing-secret}")String slackAppSigningSecret) {
        this.slackAppSigningSecret = slackAppSigningSecret;
    }

    @Bean
    public MethodsClient methodsClient() {
        return Slack.getInstance().methods(slackToken);
    }
}
