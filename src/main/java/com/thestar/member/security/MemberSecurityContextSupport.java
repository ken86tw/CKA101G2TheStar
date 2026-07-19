package com.thestar.member.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * 會員使用獨立的 SecurityContext Session Key，避免與員工後台共用
 * SPRING_SECURITY_CONTEXT 後互相誤判身分。
 */
@Component
public class MemberSecurityContextSupport {

    public static final String MEMBER_SECURITY_CONTEXT_KEY = "MEMBER_SECURITY_CONTEXT";

    private final HttpSessionSecurityContextRepository repository;

    public MemberSecurityContextSupport() {
        repository = new HttpSessionSecurityContextRepository();
        repository.setSpringSecurityContextKey(MEMBER_SECURITY_CONTEXT_KEY);
    }

    public SecurityContextRepository repository() {
        return repository;
    }

    public void login(Authentication authentication,
                      HttpServletRequest request,
                      HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        repository.saveContext(context, request, response);
    }

    public void clear(HttpServletRequest request,
                      HttpServletResponse response) {
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.clearContext();
        repository.saveContext(emptyContext, request, response);
        clear(request.getSession(false));
    }

    public void clear(HttpSession session) {
        if (session != null) {
            session.removeAttribute(MEMBER_SECURITY_CONTEXT_KEY);
        }
    }
}
