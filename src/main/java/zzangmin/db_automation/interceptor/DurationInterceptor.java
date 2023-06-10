package zzangmin.db_automation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import zzangmin.db_automation.dto.response.CreateTableResponseDTO;
import zzangmin.db_automation.dto.response.ResponseDTO;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
public class DurationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startTime", System.nanoTime());
        return true;
    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
//        long startTime = (long) request.getAttribute("startTime");
//        long endTime = System.nanoTime();
//        long executionTime = endTime - startTime;
//
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method controllerMethod = handlerMethod.getMethod();
//
//            if (controllerMethod.getReturnType().equals(ResponseDTO.class)) {
//                CreateTableResponseDTO dto = (CreateTableResponseDTO) modelAndView;
//                dto.setExecuteDuration(TimeUnit.NANOSECONDS.toMillis(executionTime));
//            }
//        }
//
//        System.out.println("executionTime = " + executionTime);
//    }

//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        if (handler instanceof HandlerMethod && response.getStatus() == HttpServletResponse.SC_OK) {
//            Object returnValue = ((HandlerMethod) handler).getReturnValue();
//
//            if (returnValue instanceof CreateTableResponseDTO) {
//                CreateTableResponseDTO dto = (CreateTableResponseDTO) returnValue;
//                // DTO에 실행 시간 설정 등 추가 작업 수행
//                long startTime = (long) request.getAttribute("startTime");
//                long endTime = System.nanoTime();
//                long executionTime = endTime - startTime;
//                dto.setExecuteDuration(TimeUnit.NANOSECONDS.toMillis(executionTime));
//            }
//        }
//    }
}
