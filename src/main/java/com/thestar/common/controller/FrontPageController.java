package com.thestar.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontPageController {

    @GetMapping({"/", "/index.html"})
    public String index() {
        return "index";
    }

    @GetMapping("/facilities.html")
    public String facilities() {
        return "facilities";
    }

    @GetMapping("/coupons.html")
    public String coupons() {
        return "coupons";
    }
}
