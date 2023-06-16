package zzangmin.db_automation.aop;


import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class ConcurrentDDLAspect {

    private Map<String, Boolean> ddlRunningDatabases = new ConcurrentHashMap<>();

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    private void requestMappingMethods() {}


    @Before("requestMappingMethods()")
    public void beforeRequest(JoinPoint joinPoint) {
        String databaseName = findDatabaseNameFromHttpRequest();
        if (isDatabaseRunningDDL(databaseName)) {
            throw new IllegalStateException("해당 DB 서버에서 이미 DDL이 실행중입니다");
        }
        addRunningDatabase(databaseName);
    }

    @After("requestMappingMethods()")
    public void afterReturningRequest(JoinPoint joinPoint) {
        String databaseName = findDatabaseNameFromHttpRequest();
        removeRunningDatabase(databaseName);
    }

    private boolean isDatabaseRunningDDL(String databaseName) {
        if (ddlRunningDatabases.containsKey(databaseName)) {
            return true;
        }
        return false;
    }

    private String findDatabaseNameFromHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String databaseName = request.getParameter("databaseName");
        return databaseName;
    }

    private void addRunningDatabase(String databaseName) {
        ddlRunningDatabases.put(databaseName, true);
    }

    private void removeRunningDatabase(String databaseName) {
        ddlRunningDatabases.remove(databaseName);
    }

}
