package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SlackController {

    @PostMapping("/slack/callback")
    public ResponseEntity<Boolean> slackCallBack(@RequestParam String payload) {


        return ResponseEntity.ok(true);
    }
}
