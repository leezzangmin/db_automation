package zzangmin.db_automation.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileUtil {

    public static String CURRENT_ENVIRONMENT_PROFILE;

    @Autowired
    public void setCURRENT_ENVIRONMENT_PROFILE(@Value("${spring.profiles.active}")String profile) {
        this.CURRENT_ENVIRONMENT_PROFILE = profile;
    }
}
