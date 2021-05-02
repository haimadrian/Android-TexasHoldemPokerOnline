package org.hit.android.haim.texasholdem.server.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Haim Adrian
 * @since 22-Mar-21
 */
@Controller
public class HomeController {
    public static final String HTML_PAGE = "<!DOCTYPE html>\n" +
        "<html style=\"height:100%\">\n" +
        "  <style>\n" +
        "  div {\n" +
        "    height: 100%;\n" +
        "    background: linear-gradient(rgb(0,148,245), rgb(12,45,85), rgb(0,148,245));\n" +
        "    padding-top: 10px;\n" +
        "    padding-bottom: 30px;\n" +
        "    padding-left: 20px;\n" +
        "  }\n" +
        "  </style>\n" +
        "  <body style=\"color:white\">\n" +
        "    <div>\n" +
        "      <h1>Welcome to Texas Holdem server</h1>\n" +
        "      <font size=\"+2\">##<br/>\n" +
        "      Meanwhile, enjoy:</font>\n" +
        "      <p style=\"text-align:center\"><iframe width=\"1080\" height=\"600\" src=\"https://www.youtube.com/embed/CgvhpvOHPFo?playlist=CgvhpvOHPFo&ab_channel=skaatharel&autoplay=1&loop=1\"></iframe></p>\n" +
        "      <font size=\"+1\">Teacher: Effi Profus the King<br/>\n" +
        "      Student: Haim Adrian the Carpenter<br/>\n" +
        "      &#169; Haim Adrian, HIT, Android1 2021 &#169;</font>\n" +
        "    </div>\n" +
        "  </body>\n" +
        "</html>";

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String homePage() {
        return HTML_PAGE.replace("##", "You have to sign in using Texas Holdem application in order to access services.");
    }

    @GetMapping(path = "/favicon.ico", produces = "image/ico")
    @ResponseBody
    public byte[] favIcon() throws IOException, URISyntaxException {
        return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("favicon.ico").toURI()));
    }
}

