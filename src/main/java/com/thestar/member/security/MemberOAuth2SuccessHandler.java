package com.thestar.member.security;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberGoogleAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MemberOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberGoogleAuthService memberGoogleAuthService;
    private final MemberSecurityContextSupport memberSecurityContextSupport;

    public MemberOAuth2SuccessHandler(MemberGoogleAuthService memberGoogleAuthService,
                                      MemberSecurityContextSupport memberSecurityContextSupport) {
        this.memberGoogleAuthService = memberGoogleAuthService;
        this.memberSecurityContextSupport = memberSecurityContextSupport;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendRedirect("/login.html?googleError=1");
            return;
        }

        HttpSession session = request.getSession(true);

        try {
            MemberVO member = memberGoogleAuthService.prepareGoogleLogin(oidcUser, session);
            if (member == null) {
                memberSecurityContextSupport.clear(request, response);
                response.sendRedirect("/google-complete-profile.html");
                return;
            }

            loginMember(member, request, response);
            String redirect = normalizeRedirect(
                    (String) session.getAttribute(MemberGoogleAuthService.MEMBER_LOGIN_REDIRECT_KEY));
            session.removeAttribute(MemberGoogleAuthService.MEMBER_LOGIN_REDIRECT_KEY);
            response.sendRedirect(redirect);
        } catch (IllegalArgumentException | IllegalStateException e) {
            memberSecurityContextSupport.clear(request, response);
            session.setAttribute("googleLoginError", e.getMessage());
            response.sendRedirect("/login.html?googleError=1");
        }
    }

    public void loginMember(MemberVO member,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        session.removeAttribute("loginEmployee");
        session.removeAttribute("SPRING_SECURITY_CONTEXT");
        session.setAttribute("loginMember", member);

        MemberUserDetails principal = new MemberUserDetails(member);
        Authentication memberAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
        memberSecurityContextSupport.login(memberAuthentication, request, response);
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
