package org.hit.android.haim.chat.server.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@RestController
public class HomeController {
    @GetMapping("/")
    public ResponseEntity<?> homePage() {
        return ResponseEntity.ok("<h1>Welcome to Android1 chat server</h1>\n" +
            "You have to use Chat android application in order to access services.");
    }

    @GetMapping(path = "/favicon.ico", produces = "image/ico")
    public byte[] favIcon() throws IOException {
        byte[] icon = new byte[32768];
        int read = IOUtils.read(getClass().getClassLoader().getResourceAsStream("favicon.ico"), icon, 0, 32768);
        return Arrays.copyOf(icon, read);
    }
}

