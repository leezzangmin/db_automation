package zzangmin.db_automation.standardvalue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginComponentStandard {
    public final static Map<String, String> pluginComponentStandardValues = new HashMap<>();

    public final static List<String> essentialPluginComponentNames = List.of("validate_password");
// component -> 'file://component_validate_password'

    static {
        pluginComponentStandardValues.put("validate_password_check_user_name", "ON");
        pluginComponentStandardValues.put("validate_password_dictionary_file", "");
        pluginComponentStandardValues.put("validate_password_length", "8");
        pluginComponentStandardValues.put("validate_password_mixed_case_count", "1");
        pluginComponentStandardValues.put("validate_password_number_count", "1");
        pluginComponentStandardValues.put("validate_password_policy", "MEDIUM");
        pluginComponentStandardValues.put("validate_password_special_char_count", "1");
    }

}
