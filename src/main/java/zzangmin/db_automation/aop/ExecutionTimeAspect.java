package zzangmin.db_automation.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.response.ResponseDTO;

@Aspect
@Component
public class ExecutionTimeAspect {
    private static final ThreadLocal<Long> executionTimeHolder = new ThreadLocal<>();

    @Autowired
    public ExecutionTimeAspect() {}

    @Pointcut(" @annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void requestMappingMethods() {}

    @Before("requestMappingMethods()")
    public void setStartTime(JoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        executionTimeHolder.set(startTime);
    }

    @Order(1)
    @AfterReturning(pointcut = "requestMappingMethods()", returning = "dto")
    public void afterReturningSetDTOsDuration(Object dto) {
        long endTime = System.currentTimeMillis();
        long startTime = executionTimeHolder.get();
        executionTimeHolder.remove();
        long duration = calculateDuration(startTime, endTime);
        ((ResponseDTO) dto).setExecuteDuration(duration);
    }

    private long calculateDuration(long startTime, long endTime) {
        return endTime - startTime;
    }

}
