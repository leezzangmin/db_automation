package zzangmin.db_automation.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.request.ddl.DDLRequestDTO;
import zzangmin.db_automation.dto.response.ddl.DDLResponseDTO;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.service.SlackService;

@RequiredArgsConstructor
@Aspect
@Component
public class DDLMessageSendAspect {

    private final SlackService slackService;


    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    private void requestMappingMethods() {}

    @Pointcut("execution(* zzangmin.db_automation.controller.DDLController.*(..))")
    private void ddlControllerMethods() {}

    @Before("requestMappingMethods() && ddlControllerMethods()")
    public void sendStartMessage(JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder();
        sb.append("<DDL Execution Start!>\n");
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof DDLRequestDTO) {
                arg = (DDLRequestDTO) arg;
                sb.append("CommandType: ");
                sb.append(((DDLRequestDTO) arg).extractCommandType());
            } else if (arg instanceof DatabaseConnectionInfo) {
                arg = (DatabaseConnectionInfo) arg;
                sb.append("connection info:");
                sb.append(((DatabaseConnectionInfo) arg).databaseSummary());
            }
        }
        slackService.sendNormalStringMessage(sb.toString());
    }

    @AfterReturning(pointcut = "requestMappingMethods() && ddlControllerMethods()", returning = "dto")
    public void afterReturningSendDTO(JoinPoint joinPoint, Object dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("<DDL Execution Finished!>\n");
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof DDLRequestDTO) {
                sb.append("CommandType: ");
                sb.append(((DDLRequestDTO) arg).extractCommandType());
            } else if (arg instanceof DatabaseConnectionInfo) {
                arg = (DatabaseConnectionInfo) arg;
                sb.append("connection info:");
                sb.append(((DatabaseConnectionInfo) arg).databaseSummary());
            }
        }
        sb.append("\n");
        sb.append(((DDLResponseDTO) dto).toString());
        slackService.sendNormalStringMessage(sb.toString());
    }

    @AfterThrowing(pointcut = "requestMappingMethods() && ddlControllerMethods()", throwing = "error")
    public void sendErrorMessag(JoinPoint joinPoint, Throwable error) {
        StringBuilder sb = new StringBuilder();
        sb.append("<DDL Execution Failed!>\n");
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof DDLRequestDTO) {
                sb.append("CommandType: ");
                sb.append(((DDLRequestDTO) arg).extractCommandType());
            } else if (arg instanceof DatabaseConnectionInfo) {
                arg = (DatabaseConnectionInfo) arg;
                sb.append("connection info:");
                sb.append(((DatabaseConnectionInfo) arg).databaseSummary());
            }
        }
        sb.append("\nError Message: ");
        sb.append(error.getMessage());

        slackService.sendNormalStringMessage(sb.toString());
    }

}
