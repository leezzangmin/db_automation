package zzangmin.db_automation.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static Object toObject(String jsonString) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, Object.class);
    }

}
