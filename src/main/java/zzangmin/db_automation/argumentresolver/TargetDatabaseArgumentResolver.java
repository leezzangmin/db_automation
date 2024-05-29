package zzangmin.db_automation.argumentresolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import zzangmin.db_automation.dto.DatabaseConnectionInfo;
import zzangmin.db_automation.config.DynamicDataSourceProperties;


@RequiredArgsConstructor
@Component
public class TargetDatabaseArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthAnnotation = parameter.hasParameterAnnotation(TargetDatabase.class);
        boolean hasDatabaseConnectionInfo = DatabaseConnectionInfo.class.isAssignableFrom(parameter.getParameterType());
        return hasAuthAnnotation && hasDatabaseConnectionInfo;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String databaseNameInput = request.getParameter("databaseName");
        return DynamicDataSourceProperties.findByDbIdentifier(databaseNameInput);
    }
}
