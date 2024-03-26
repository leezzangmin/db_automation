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
    private final int MAX_MESSAGE_SIZE = 10000;

    public void sendMessage(String message) {
        if (message.isBlank()) {
            return;
        }

        for (int start = 0; start < message.length(); start += MAX_MESSAGE_SIZE) {
            int end = Math.min(message.length(), start + MAX_MESSAGE_SIZE);
            String messageChunk = getChunk(message, start, end);
            log.info("Sending message chunk: {}", messageChunk);

            SlackMessageDTO slackMessage = new SlackMessageDTO(messageChunk);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SlackMessageDTO> entity = new HttpEntity<>(slackMessage, headers);
            restTemplate.postForEntity(slackWebhookUrl, entity, String.class);
        }
    }

    private String getChunk(String message, int start, int end) {
        return message.substring(start, end);
    }
}
