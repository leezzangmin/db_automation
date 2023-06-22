package zzangmin.db_automation.argumentresolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import zzangmin.db_automation.info.DatabaseConnectionInfo;
import zzangmin.db_automation.config.DynamicDataSourceProperties;


@RequiredArgsConstructor
@Component
public class TargetDatabaseArgumentResolver implements HandlerMethodArgumentResolver {

    private final DynamicDataSourceProperties dataSourceProperties;

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
        if (databaseNameInput == null || databaseNameInput == "") {
            throw new IllegalArgumentException("올바른 DB명 입력이 아닙니다.");
        }
        return dataSourceProperties.findByDbName(databaseNameInput);
    }
}
