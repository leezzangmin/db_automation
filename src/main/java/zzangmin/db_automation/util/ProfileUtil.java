package zzangmin.db_automation.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileUtil {

    @Value("${spring.profiles.active}")
    public String CURRENT_ENVIRONMENT_PROFILE;

}
