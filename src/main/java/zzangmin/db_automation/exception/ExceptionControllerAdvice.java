package zzangmin.db_automation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    public String globalException(Exception e) {
        log.error("Exception Occurred: {}", e.getMessage());
        return "ERROR. "+ e.getMessage().toString();
    }
}
