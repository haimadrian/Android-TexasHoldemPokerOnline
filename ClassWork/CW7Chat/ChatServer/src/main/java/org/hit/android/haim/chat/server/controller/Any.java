package org.hit.android.haim.chat.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * For any unknown path, just say hello and avoid of Spring boot errors.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@RestController
@RequestMapping("/*")
public class Any {
    @GetMapping
    public ResponseEntity<?> any() {
        return ResponseEntity.ok("<h1>Welcome to Android1 chat server</h1>\nThis page does not exist");
    }
}

