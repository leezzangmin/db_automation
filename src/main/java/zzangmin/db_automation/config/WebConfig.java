package zzangmin.db_automation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zzangmin.db_automation.argumentresolver.RequestUUIDArgumentResolver;
import zzangmin.db_automation.argumentresolver.TargetDatabaseArgumentResolver;
import zzangmin.db_automation.interceptor.DurationInterceptor;
import zzangmin.db_automation.interceptor.ExecutableTimeInterceptor;
import zzangmin.db_automation.interceptor.UUIDInterceptor;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final TargetDatabaseArgumentResolver targetDatabaseArgumentResolver;
    private final RequestUUIDArgumentResolver requestUUIDArgumentResolver;
    private final UUIDInterceptor uuidInterceptor;
    private final DurationInterceptor durationInterceptor;
    private final ExecutableTimeInterceptor executableTimeInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(targetDatabaseArgumentResolver);
        resolvers.add(requestUUIDArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(uuidInterceptor);
        registry.addInterceptor(durationInterceptor);
        registry.addInterceptor(executableTimeInterceptor);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
