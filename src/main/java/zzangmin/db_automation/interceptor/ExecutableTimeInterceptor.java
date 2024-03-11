package zzangmin.db_automation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalTime;

@Component
public class ExecutableTimeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(00, 0);
        LocalTime endTime = LocalTime.of(23, 59);

        if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
            throw new IllegalStateException("현재 시간에는 요청을 처리할 수 없습니다.\n실행 가능시간:" + startTime + " ~ " + endTime);
        }

        return true;
    }
}
