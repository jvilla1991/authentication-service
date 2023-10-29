package com.moo.authenticationservice.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/utils-controller")
public class DemoController {

    @GetMapping
    public ResponseEntity<String> ping () {
        return ResponseEntity.ok("pong");
    }
}
