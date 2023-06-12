//package zzangmin.db_automation.validator;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.time.*;
//
//@RequiredArgsConstructor
//@Component
//public class TimeValidator {
//    private final static String zoneId = "Asia/Seoul";
//    private final ZonedDateTime executableStartTime = ZonedDateTime.of(
//            LocalDate.now(),
//            LocalTime.of(9, 0),
//            ZoneId.of(zoneId)
//    );
//    private final ZonedDateTime executableEndTime = ZonedDateTime.of(
//            LocalDate.now(),
//            LocalTime.of(17, 0),
//            ZoneId.of(zoneId)
//    );
//
//    public void validateExecutableTime(ZonedDateTime currentTime) {
//        //ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(zoneId));
//        if (currentTime.isAfter(executableStartTime) && currentTime.isBefore(executableEndTime)) {
//            return;
//        }
//        throw new IllegalStateException("명령 실행가능 시간이 아닙니다.");
//    }
//}
