//package zzangmin.db_automation.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//import zzangmin.db_automation.client.SlackClient;
//
//@Configuration
//public class SlackConfig {
//
//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//
//    @Bean
//    public SlackClient slackService(RestTemplate restTemplate) {
//        return new SlackClient(restTemplate);
//    }
//
//}
