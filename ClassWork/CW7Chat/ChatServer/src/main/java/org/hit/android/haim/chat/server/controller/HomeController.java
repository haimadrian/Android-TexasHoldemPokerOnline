package org.hit.android.haim.chat.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    public byte[] favIcon() throws IOException, URISyntaxException {
        return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("favicon.ico").toURI()));
    }
}

