package zzangmin.db_automation.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final MetadataLockDetector metadataLockDetector;

    @Pointcut("execution(* zzangmin.db_automation.service.DDLService.*(..))")
    public void ddlServiceMethods() {}

    @Before("ddlServiceMethods()")
    public void startCheckMetadataLock(JoinPoint joinPoint) {
        DatabaseConnectionInfo databaseConnectionInfo = null;
        Object[] methodArgs = joinPoint.getArgs();
        for (Object arg : methodArgs) {
            if (arg instanceof DatabaseConnectionInfo) {
                databaseConnectionInfo = (DatabaseConnectionInfo) arg;
            }
        }
        metadataLockDetector.startCheck(databaseConnectionInfo);
    }

    @After("ddlServiceMethods()")
    public void endCheckMetadataLock(JoinPoint joinPoint) {
        DatabaseConnectionInfo databaseConnectionInfo = null;
        Object[] methodArgs = joinPoint.getArgs();
        for (Object arg : methodArgs) {
            if (arg instanceof DatabaseConnectionInfo) {
                databaseConnectionInfo = (DatabaseConnectionInfo) arg;
            }
        }
        metadataLockDetector.endCheck(databaseConnectionInfo);
    }

}
