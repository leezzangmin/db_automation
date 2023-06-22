//package zzangmin.db_automation.filter;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import zzangmin.db_automation.dto.response.ResponseDTO;
//
//import java.io.IOException;
//
//
//public class ExecutionTimeCheckFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        // HttpServletRequest와 HttpServletResponse로 캐스팅
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        // 필터링된 요청 처리
//
//        // 필터가 적용되는 컨트롤러 메서드 실행 후 반환된 객체를 가져옴
//        chain.doFilter(request, response);
//
//        // 반환된 객체가 DTO인 경우 setter를 통해 값을 주입
//        if (responseObject instanceof ResponseDTO) {
//            ResponseDTO dto = (ResponseDTO) responseObject;
//            dto.setExecuteDuration(10.5); // 값을 주입하거나 계산된 값을 설정할 수 있습니다.
//        }
//        // 컨트롤러 메서드 실행 후, DTO에 값을 주입
//        ResponseDTO dto = (ResponseDTO) httpRequest.getAttribute("dto");
//        if (dto != null) {
//            dto.setExecuteDuration(1);
//        }
//    }
//}
