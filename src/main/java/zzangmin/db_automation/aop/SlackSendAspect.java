package zzangmin.db_automation.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.client.SlackClient;
import zzangmin.db_automation.dto.response.DDLResponseDTO;

@Aspect
@Component
public class SlackSendAspect {

    private final SlackClient slackClient;
    @Autowired
    public SlackSendAspect(SlackClient slackClient) {
        this.slackClient = slackClient;
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    private void requestMappingMethods() {}

    @AfterReturning(pointcut = "requestMappingMethods()", returning = "dto")
    public void afterReturningSendDTO(Object dto) {
       slackClient.sendMessage(((DDLResponseDTO) dto).toString());
    }

}
