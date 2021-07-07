package org.hit.android.haim.texasholdem.server.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Haim Adrian
 * @since 22-Mar-21
 */
@Controller
public class HomeController {
    public static final String HTML_PAGE;

    static {
        String content;
        try {
            InputStream resourceAsStream = HomeController.class.getClassLoader().getResourceAsStream("templates/index.html");
            if (resourceAsStream != null) {
                content = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            } else {
                content = "##";
            }
        } catch (Exception e) {
            content = "##";
        }

        HTML_PAGE = content;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String homePage() {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<body>\n" +
            "<div class=\"content\">\n" +
            "    <h1>Welcome</h1>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";
        //return HTML_PAGE.replace("##", "You have to sign in using Texas Holdem application in order to access services.");
    }

    @GetMapping(path = "/favicon.ico", produces = "image/ico")
    @ResponseBody
    public byte[] favIcon() throws IOException {
        byte[] icon = new byte[32768];
        int read = IOUtils.read(getClass().getClassLoader().getResourceAsStream("favicon.ico"), icon, 0, 32768);
        return Arrays.copyOf(icon, read);
    }
}

