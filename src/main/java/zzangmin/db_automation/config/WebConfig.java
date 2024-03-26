package zzangmin.db_automation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zzangmin.db_automation.argumentresolver.TargetDatabaseArgumentResolver;
import zzangmin.db_automation.interceptor.ExecutableTimeInterceptor;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final TargetDatabaseArgumentResolver targetDatabaseArgumentResolver;
    private final ExecutableTimeInterceptor executableTimeInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(targetDatabaseArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(executableTimeInterceptor);
    }

}
