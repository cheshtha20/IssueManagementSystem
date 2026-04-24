package com.issuemanage.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle Single Page Application (SPA) routing.
 * Any request that is not an API call or a static resource (file with an extension)
 * is forwarded to index.html to allow React Router to handle the route.
 */
@Controller
public class ForwardController {

    @RequestMapping(value = {
        "/{path:[^\\.]*}",
        "/**/{path:[^\\.]*}"
    })
    public String forward(jakarta.servlet.http.HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api") || path.startsWith("/h2-console")) {
            return "forward:" + path; // Let Spring handle it normally
        }
        return "forward:/index.html";
    }
}
