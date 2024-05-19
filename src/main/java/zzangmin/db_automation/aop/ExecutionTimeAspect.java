package zzangmin.db_automation.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.response.DDLResponseDTO;

@Aspect
@Component
public class ExecutionTimeAspect {
    private static final ThreadLocal<Long> executionTimeHolder = new ThreadLocal<>();

    @Autowired
    public ExecutionTimeAspect() {}

    @Pointcut("execution(* zzangmin.db_automation.controller.DDLController.*(..))")
    private void ddlControllerMethods() {}

    @Before("ddlControllerMethods()")
    public void setStartTime(JoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        executionTimeHolder.set(startTime);
    }

    @AfterReturning(pointcut = "ddlControllerMethods()", returning = "dto")
    public void afterReturningSetDTOsDuration(Object dto) {
        long endTime = System.currentTimeMillis();
        long startTime = executionTimeHolder.get();
        executionTimeHolder.remove();
        long duration = calculateDuration(startTime, endTime);
        ((DDLResponseDTO) dto).setExecuteDuration(duration);
    }

    private long calculateDuration(long startTime, long endTime) {
        return endTime - startTime;
    }

}
