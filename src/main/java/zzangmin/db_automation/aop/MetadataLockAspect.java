//package zzangmin.db_automation.aop;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//public class MetadataLockAspect {
//    private static final long MAX_RESPONSE_TIME_MILLISECONDS = 3000;
//
//    @Pointcut("execution(* com.example.YourServiceClass.addColumn(..))")
//    public void addColumnPointcut() {
//        // 포인트컷 설정: addColumn 메소드에 대한 포인트컷
//    }
//
//    @Around("addColumnPointcut()")
//    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//
//        Object result = joinPoint.proceed(); // 원래 메소드 실행
//
//        long endTime = System.currentTimeMillis();
//        long executionTime = endTime - startTime;
//
//        if (executionTime >= MAX_RESPONSE_TIME_MILLISECONDS) {
//            // 응답 지연이 발생한 경우 DDL 문장 또는 클라이언트 요청을 종료하는 로직을 추가합니다.
//            // 이 예시에서는 DDL 문장을 종료하는 예외를 던지도록 하였습니다.
//            throw new IllegalStateException("DDL execution time exceeded the maximum limit");
//        }
//
//        return result;
//    }
//
//}
