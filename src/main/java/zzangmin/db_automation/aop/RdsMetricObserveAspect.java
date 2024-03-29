package zzangmin.db_automation.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.schedule.RdsMetricDetector;

@RequiredArgsConstructor
@Aspect
@Component
public class RdsMetricObserveAspect {

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
