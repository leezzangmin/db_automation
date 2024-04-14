package zzangmin.db_automation.config;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class SlackConfig {

    @Value("${slack.token}")
    public String slackToken;

    @Value("${slack.webbhook-url}")
    private String SLACK_WEBHOOK_URL;

    public static String DEFAULT_CHANNEL_ID = "C04282D342D"; // TODO: env

    public static final int MAX_MESSAGE_SIZE = 10000;

    @Bean
    public MethodsClient methodsClient() {
        return Slack.getInstance().methods(slackToken);
    }
}
