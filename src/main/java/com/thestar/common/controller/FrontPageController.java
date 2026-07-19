package com.thestar.common.controller;

import com.thestar.content.service.ContentAdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontPageController {

    private final ContentAdminService contentAdminService;

    public FrontPageController(ContentAdminService contentAdminService) {
        this.contentAdminService = contentAdminService;
    }

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        model.addAttribute("latestNews", contentAdminService.findLatestNews());
        return "index";
    }
    
    @GetMapping("/about.html")
    public String about() {
        return "about";
    }

    @GetMapping("/facilities.html")
    public String facilities() {
        return "facilities";
    }

    @GetMapping("/articles.html")
    public String articles() {
        return "articles";
    }

    @GetMapping("/coupons.html")
    public String coupons() {
        return "coupons";
    }

    /**
     * 訂房頁:從 static 搬進 templates 後改由這裡回傳,才能用 Thymeleaf 嵌入共用導覽列。
     * 員工登入時(AdminAuthenticationSuccessHandler 會在 session 存 loginEmployee)
     * isEmployee 為 true,模板就不渲染前台共用導覽列,維持原本的員工操作介面。
     */
    @GetMapping("/roombooking.html")
    public String roombooking(HttpSession session, Model model) {
        model.addAttribute("isEmployee", session.getAttribute("loginEmployee") != null);
        return "roombooking";
    }
}
