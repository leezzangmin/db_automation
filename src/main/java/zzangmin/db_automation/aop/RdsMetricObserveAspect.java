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
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String databaseName = request.getParameter("databaseName");
        DatabaseConnectionInfo databaseConnectionInfo = dynamicDataSourceProperties.findByDbName(databaseName);
        rdsMetricDetector.startCheck(databaseConnectionInfo);
    }

    @After("ddlServiceMethods()")
    public void endCheckRdsMetric(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String databaseName = request.getParameter("databaseName");
        DatabaseConnectionInfo databaseConnectionInfo = dynamicDataSourceProperties.findByDbName(databaseName);
        rdsMetricDetector.endCheck(databaseConnectionInfo);
    }

}
