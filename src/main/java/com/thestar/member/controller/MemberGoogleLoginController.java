package com.thestar.member.controller;

import com.thestar.member.service.MemberGoogleAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberGoogleLoginController {

    @Value("${app.google-login.enabled:false}")
    private boolean googleLoginEnabled;

    @GetMapping("/member/google/login")
    public String startGoogleLogin(
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpSession session) {
        if (!googleLoginEnabled) {
            return "redirect:/login.html?googleDisabled=1";
        }

        session.setAttribute(
                MemberGoogleAuthService.MEMBER_LOGIN_REDIRECT_KEY,
                normalizeRedirect(redirect)
        );
        return "redirect:/oauth2/authorization/google";
    }

    private String normalizeRedirect(String value) {
        if (value == null || value.isBlank() || !value.startsWith("/") || value.startsWith("//")) {
            return "/index.html";
        }
        if (value.startsWith("/api/") || value.startsWith("/thestar/")) {
            return "/index.html";
        }
        return value;
    }
}
