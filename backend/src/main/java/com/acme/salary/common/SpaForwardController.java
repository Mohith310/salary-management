package com.acme.salary.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Angular handles these URLs in the browser. When someone refreshes or opens a deep link, the server must
 * hand back index.html instead of a 404 so the Angular router can take over.
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/employees", "/employees/**", "/insights", "/insights/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
