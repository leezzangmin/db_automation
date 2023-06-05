package zzangmin.db_automation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DescribeController {

    @GetMapping("/describe/rds/{instanceIdentifier}")
    public String describeRDSInstanceResource(String instanceIdentifier) {
        return "ok";
    }

    @GetMapping("/ping")
    public List<String> healthCheck() {
        return Arrays.asList("hello", LocalDateTime.now().toString());
    }
}
