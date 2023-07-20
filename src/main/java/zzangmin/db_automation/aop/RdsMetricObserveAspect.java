package zzangmin.db_automation.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zzangmin.db_automation.config.DynamicDataSourceProperties;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.RdsMetricDetector;

@RequiredArgsConstructor
@Aspect
@Component
public class RdsMetricObserveAspect {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;
    private final RdsMetricDetector rdsMetricDetector;

    @Pointcut("execution(* zzangmin.db_automation.service.DDLService.*(..))")
    public void ddlServiceMethods() {
    }

    @Before("ddlServiceMethods()")
    public void startCheckRdsMetric(JoinPoint joinPoint) {
        DatabaseConnectionInfo databaseConnectionInfo = null;
        Object[] methodArgs = joinPoint.getArgs();
        for (Object arg : methodArgs) {
            if (arg instanceof DatabaseConnectionInfo) {
                databaseConnectionInfo = (DatabaseConnectionInfo) arg;
            }
        }
        rdsMetricDetector.startCheck(databaseConnectionInfo);
    }

    @After("ddlServiceMethods()")
    public void endCheckRdsMetric(JoinPoint joinPoint) {
        DatabaseConnectionInfo databaseConnectionInfo = null;
        Object[] methodArgs = joinPoint.getArgs();
        for (Object arg : methodArgs) {
            if (arg instanceof DatabaseConnectionInfo) {
                databaseConnectionInfo = (DatabaseConnectionInfo) arg;
            }
        }

        rdsMetricDetector.endCheck(databaseConnectionInfo);
    }

}
