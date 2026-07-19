package com.thestar.member.controller;

import com.thestar.member.dto.GoogleCompleteProfileRequest;
import com.thestar.member.dto.MemberForgotPasswordRequest;
import com.thestar.member.dto.MemberLoginRequest;
import com.thestar.member.dto.MemberProfileDTO;
import com.thestar.member.dto.MemberProfileUpdateRequest;
import com.thestar.member.dto.MemberRegisterRequest;
import com.thestar.member.dto.MemberRegisterResponse;
import com.thestar.member.dto.MemberResetPasswordRequest;
import com.thestar.member.dto.MemberSessionDTO;
import com.thestar.member.dto.MemberVerifyResponse;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.security.MemberSecurityContextSupport;
import com.thestar.member.security.MemberUserDetails;
import com.thestar.member.service.MemberAuthService;
import com.thestar.member.service.MemberGoogleAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberAuthController {

    private final MemberAuthService memberAuthService;
    private final MemberGoogleAuthService memberGoogleAuthService;
    private final AuthenticationManager memberAuthenticationManager;
    private final MemberSecurityContextSupport memberSecurityContextSupport;

    @Value("${app.google-login.enabled:false}")
    private boolean googleLoginEnabled;

    public MemberAuthController(MemberAuthService memberAuthService,
                                MemberGoogleAuthService memberGoogleAuthService,
                                @Qualifier("memberAuthenticationManager")
                                AuthenticationManager memberAuthenticationManager,
                                MemberSecurityContextSupport memberSecurityContextSupport) {
        this.memberAuthService = memberAuthService;
        this.memberGoogleAuthService = memberGoogleAuthService;
        this.memberAuthenticationManager = memberAuthenticationManager;
        this.memberSecurityContextSupport = memberSecurityContextSupport;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@ModelAttribute MemberRegisterRequest request) {
        try {
            MemberRegisterResponse response = memberAuthService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "會員照片讀取失敗"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        try {
            MemberVerifyResponse response = memberAuthService.verifyEmail(token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String memberEmail = request.get("memberEmail");
            MemberRegisterResponse response = memberAuthService.resendVerificationMail(memberEmail);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        String email = request.getMemberEmail() == null ? "" : request.getMemberEmail().trim();
        String password = request.getMemberPassword() == null ? "" : request.getMemberPassword();

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請輸入信箱與密碼"));
        }

        try {
            Authentication authentication = memberAuthenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, password));

            MemberUserDetails principal = (MemberUserDetails) authentication.getPrincipal();
            MemberVO member = memberAuthService.getMemberForAuthenticatedLogin(principal.getMemberId());

            HttpSession session = httpRequest.getSession(true);
            if (!session.isNew()) {
                httpRequest.changeSessionId();
            }
            session.removeAttribute("loginEmployee");
            session.removeAttribute("SPRING_SECURITY_CONTEXT");
            session.setAttribute("loginMember", member);
            memberSecurityContextSupport.login(authentication, httpRequest, httpResponse);

            return ResponseEntity.ok(MemberSessionDTO.from(member));
        } catch (AuthenticationException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "信箱或密碼錯誤"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public MemberSessionDTO status(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        if (member == null || member.getMemberId() == null) {
            return MemberSessionDTO.guest();
        }
        return MemberSessionDTO.from(member);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("loginMember");
            session.removeAttribute("loginEmployee");
            session.removeAttribute(MemberGoogleAuthService.PENDING_GOOGLE_MEMBER_KEY);
        }
        memberSecurityContextSupport.clear(request, response);
        return Map.of("ok", true);
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@RequestBody MemberForgotPasswordRequest request) {
        memberAuthService.requestPasswordReset(request.getMemberEmail());
        return Map.of(
                "ok", true,
                "message", "若此信箱已註冊，我們將寄送密碼重設信"
        );
    }

    @GetMapping("/reset-password/validate")
    public Map<String, Object> validateResetToken(@RequestParam("token") String token) {
        return Map.of("valid", memberAuthService.isResetTokenValid(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody MemberResetPasswordRequest request) {
        try {
            memberAuthService.resetPassword(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "密碼重設成功，請使用新密碼登入"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/google/enabled")
    public Map<String, Object> googleEnabled() {
        return Map.of("enabled", googleLoginEnabled);
    }

    @GetMapping("/google/pending")
    public ResponseEntity<?> googlePending(HttpSession session) {
        try {
            return ResponseEntity.ok(memberGoogleAuthService.getPendingProfile(session));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/google/complete")
    public ResponseEntity<?> completeGoogleProfile(@RequestBody GoogleCompleteProfileRequest request,
                                                   HttpServletRequest httpRequest,
                                                   HttpServletResponse httpResponse) {
        try {
            HttpSession session = httpRequest.getSession(true);
            MemberVO member = memberGoogleAuthService.completeGoogleProfile(request, session);

            session.removeAttribute("loginEmployee");
            session.removeAttribute("SPRING_SECURITY_CONTEXT");
            session.setAttribute("loginMember", member);

            MemberUserDetails principal = new MemberUserDetails(member);
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            memberSecurityContextSupport.login(authentication, httpRequest, httpResponse);

            String redirect = normalizeRedirect(
                    (String) session.getAttribute(MemberGoogleAuthService.MEMBER_LOGIN_REDIRECT_KEY));
            session.removeAttribute(MemberGoogleAuthService.MEMBER_LOGIN_REDIRECT_KEY);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("ok", true);
            result.put("member", MemberSessionDTO.from(member));
            result.put("redirect", redirect);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        MemberVO member = memberAuthService.getMemberById(loginMember.getMemberId());
        return ResponseEntity.ok(MemberProfileDTO.from(member));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody MemberProfileUpdateRequest request,
                                           HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        try {
            MemberVO updatedMember = memberAuthService.updateProfile(loginMember.getMemberId(), request);
            session.setAttribute("loginMember", updatedMember);
            return ResponseEntity.ok(MemberProfileDTO.from(updatedMember));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/picture")
    public ResponseEntity<?> profilePicture(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        MemberVO member = memberAuthService.getMemberById(loginMember.getMemberId());
        byte[] picture = member.getMemberPicture();
        if (picture == null || picture.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "尚未上傳會員照片"));
        }

        return ResponseEntity.ok()
                .contentType(detectPictureMediaType(picture))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(picture);
    }

    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(@RequestParam("picture") MultipartFile picture,
                                                  HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null || loginMember.getMemberId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入會員"));
        }

        try {
            MemberVO updatedMember = memberAuthService.updateProfilePicture(loginMember.getMemberId(), picture);
            session.setAttribute("loginMember", updatedMember);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "照片儲存失敗"));
        }
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

    private MediaType detectPictureMediaType(byte[] picture) {
        if (picture.length >= 3
                && (picture[0] & 0xff) == 0xff
                && (picture[1] & 0xff) == 0xd8
                && (picture[2] & 0xff) == 0xff) {
            return MediaType.IMAGE_JPEG;
        }
        if (picture.length >= 8
                && (picture[0] & 0xff) == 0x89
                && picture[1] == 0x50
                && picture[2] == 0x4e
                && picture[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }
        if (picture.length >= 6
                && picture[0] == 0x47
                && picture[1] == 0x49
                && picture[2] == 0x46) {
            return MediaType.IMAGE_GIF;
        }
        if (picture.length >= 12
                && picture[0] == 0x52
                && picture[1] == 0x49
                && picture[2] == 0x46
                && picture[3] == 0x46
                && picture[8] == 0x57
                && picture[9] == 0x45
                && picture[10] == 0x42
                && picture[11] == 0x50) {
            return MediaType.valueOf("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
