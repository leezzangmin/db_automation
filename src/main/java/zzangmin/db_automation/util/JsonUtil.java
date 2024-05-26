package zzangmin.db_automation.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T toObject(String jsonString, Class<T> valueType) throws JsonProcessingException {
        System.out.println("jsonString = " + jsonString);
        if (jsonString == null) {
            return null;
        }
        return objectMapper.readValue(jsonString, valueType);
    }

}
