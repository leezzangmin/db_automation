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
    public static String verificationToken;

    public static String DEFAULT_CHANNEL_ID;
    public static final int MAX_MESSAGE_SIZE = 10000;

    @Autowired
    public void setSlackAppSigningSecret(@Value("${slack.app-signing-secret}")String slackAppSigningSecret,
                                         @Value("${slack.default-channel-id}")String DEFAULT_CHANNEL_ID,
                                         @Value("${slack.verification-token}")String verificationToken) {
        this.slackAppSigningSecret = slackAppSigningSecret;
        this.DEFAULT_CHANNEL_ID = DEFAULT_CHANNEL_ID;
        this.verificationToken = verificationToken;
    }

    @Bean
    public MethodsClient methodsClient() {
        return Slack.getInstance().methods(slackToken);
    }
}
