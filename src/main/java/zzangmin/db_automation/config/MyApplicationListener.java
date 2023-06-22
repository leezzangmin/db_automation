package zzangmin.db_automation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 스프링 컨테이너 초기화 후 수행되어야 하는 로직이 메서드에 들어가면 됨
 */

@RequiredArgsConstructor
@Component
public class MyApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private final DynamicDataSourceLoader dynamicDataSourceLoader;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        dynamicDataSourceLoader.loadDynamicDataSources();
    }
}
