package zzangmin.db_automation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zzangmin.db_automation.dto.response.SlackMessageDTO;

import org.springframework.http.HttpHeaders;

@Slf4j
@RequiredArgsConstructor
@Component
public class SlackClient {

    private final static String slackWebhookUrl = System.getenv("SLACK_WEBHOOK_URL");
    private final RestTemplate restTemplate;

    public void sendMessage(String message) {
        if (message.isBlank()) {
            return;
        }

        SlackMessageDTO slackMessage = new SlackMessageDTO(message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("message: {}", message);
        HttpEntity<SlackMessageDTO> entity = new HttpEntity<>(slackMessage, headers);
        restTemplate.postForEntity(slackWebhookUrl, entity, String.class);
    }
}
