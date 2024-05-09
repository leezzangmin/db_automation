package zzangmin.db_automation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ErrorMessage globalException(Exception e) {
        log.error("Exception Occurred: {}", e.getMessage());
        log.error("Exception StackTrace: {}", Arrays.toString(e.getStackTrace()));
        e.printStackTrace();
        return new ErrorMessage("ERROR. "+ e.getMessage().toString());
    }

    @Getter
    @AllArgsConstructor
    static class ErrorMessage {
        private String errorMessage;
    }
}
