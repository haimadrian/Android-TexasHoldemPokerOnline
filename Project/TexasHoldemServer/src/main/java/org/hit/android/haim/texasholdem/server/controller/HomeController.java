package org.hit.android.haim.texasholdem.server.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Haim Adrian
 * @since 22-Mar-21
 */
@Controller
public class HomeController {
    public static final String HTML_PAGE = "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<style>\n" +
        "    html, body {\n" +
        "        min-height: 100%;\n" +
        "        min-width: 100%;\n" +
        "        height: 100%;\n" +
        "        width: 100%;\n" +
        "        padding: 0;\n" +
        "        margin: 0;\n" +
        "    }\n" +
        "\n" +
        "    div {\n" +
        "        height: 100%;\n" +
        "        background: linear-gradient(rgb(0, 148, 245), rgb(12, 45, 85), rgb(0, 148, 245));\n" +
        "        padding-top: 10px;\n" +
        "        padding-bottom: 10px;\n" +
        "        padding-left: 30px;\n" +
        "    }\n" +
        "</style>\n" +
        "<body style=\"color:white\">\n" +
        "<div>\n" +
        "    <h1>Welcome to Texas Holdem Server</h1>\n" +
        "    <font size=\"+2\">##<br/>\n" +
        "    Meanwhile, enjoy:</font>\n" +
        "    <p style=\"text-align:center\"><iframe width=\"1080\" height=\"600\" src=\"https://www.youtube.com/embed/CgvhpvOHPFo?playlist=CgvhpvOHPFo&ab_channel=skaatharel&autoplay=1&loop=1\"></iframe></p>\n" +
        "    <font size=\"+1\">Teacher: Effi Profus the King<br/>\n" +
        "        Student: Haim Adrian the Carpenter<br/>\n" +
        "        &#169; Haim Adrian, HIT, Android1 2021 &#169;</font>\n" +
        "</div>\n" +
        "</body>\n" +
        "</html>\n";

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String homePage() {
        return HTML_PAGE.replace("##", "You have to sign in using Texas Holdem application in order to access services.");
    }

    @GetMapping(path = "/favicon.ico", produces = "image/ico")
    @ResponseBody
    public byte[] favIcon() throws IOException {
        byte[] icon = new byte[32768];
        int read = IOUtils.read(getClass().getClassLoader().getResourceAsStream("favicon.ico"), icon, 0, 32768);
        return Arrays.copyOf(icon, read);
    }
}

