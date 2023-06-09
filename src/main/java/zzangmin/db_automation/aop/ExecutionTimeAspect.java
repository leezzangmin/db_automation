package zzangmin.db_automation.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class ExecutionTimeAspect {
    private final ThreadLocal<Long> executionTimeThreadLocal = new ThreadLocal<>();
    private final Map<Long, Long> executionTimeMap = new HashMap<>();

    @Autowired
    public ExecutionTimeAspect() {}

    @Pointcut("@within(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestMappingMethods() {

    }

    @Around("requestMappingMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("measure 호출됨");
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        executionTimeThreadLocal.set(executionTime);
        System.out.println("executionTime = " + executionTime);
        return proceed;
    }

    public long getExecutionTime() {
        return executionTimeThreadLocal.get();
    }
}