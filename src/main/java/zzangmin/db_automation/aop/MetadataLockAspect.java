package zzangmin.db_automation.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.MetadataLockDetector;

@RequiredArgsConstructor
@Aspect
@Component
public class MetadataLockAspect {
    private static final long MAX_RESPONSE_TIME_MILLISECONDS = 3000;
    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final MetadataLockDetector metadataLockDetector;

    @Pointcut("execution(* zzangmin.db_automation.service.DDLService.*(..))")
    public void ddlServiceMethods() {
        // 포인트컷 설정: addColumn 메소드에 대한 포인트컷
    }

    @Before("ddlServiceMethods()")
    public void startCheckMetadataLock(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String databaseName = request.getParameter("databaseName");
        DatabaseConnectionInfo databaseConnectionInfo = dynamicDataSourceProperties.findByDbName(databaseName);
        metadataLockDetector.startCheck(databaseConnectionInfo);
    }

    @After("ddlServiceMethods()")
    public void endCheckMetadataLock(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String databaseName = request.getParameter("databaseName");
        DatabaseConnectionInfo databaseConnectionInfo = dynamicDataSourceProperties.findByDbName(databaseName);
        metadataLockDetector.endCheck(databaseConnectionInfo);
    }

}
