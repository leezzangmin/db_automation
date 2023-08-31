package zzangmin.db_automation.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zzangmin.db_automation.dto.response.SlackMessageDTO;

import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
@Component
public class SlackClient {

    @Value("${SLACK_WEBHOOK_URL}")
    private String slackWebhookUrl;

    private final RestTemplate restTemplate;

    public void sendMessage(String message) {
        SlackMessageDTO slackMessage = new SlackMessageDTO(message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SlackMessageDTO> entity = new HttpEntity<>(slackMessage, headers);
        restTemplate.postForEntity(slackWebhookUrl, entity, String.class);
    }
}
