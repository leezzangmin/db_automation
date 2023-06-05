package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzangmin.db_automation.entity.DBType;

/**
 * admin(DBA)만 접근할 수 있는 컨트롤러.
 * 관리하고 있는 모든 DB에 접근해서 변수 값을 긁어오는 등의 작업에 활용
 * ex1) 모든 클러스터의 binlog retention hours 를 커맨드 한 번으로 모두 불러옴
 * ex2) 모든 클러스터의 binlog retention hours 를 커맨드 한 번으로 일괄 설정
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class AdminController {

    @GetMapping("/admin")
    public void getResource(@RequestParam("SQL") String SQL, DBType dbType) {

    }

    @PostMapping("/admin")
    public void setResource() {

    }
}
