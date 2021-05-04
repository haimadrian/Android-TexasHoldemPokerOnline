package org.hit.android.haim.texasholdem.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * For any unknown path, just say hello and avoid of Spring boot errors.
 *
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@Controller
@RequestMapping("/*")
public class Any {
    @GetMapping
    @ResponseBody
    public String any() {
        return HomeController.HTML_PAGE.replace("##", "This page does not exist. <a href=\"/\">Go Home</a>");
    }
}

