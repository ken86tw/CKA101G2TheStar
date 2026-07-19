package com.thestar.member.service;

import com.thestar.member.dto.GoogleCompleteProfileRequest;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import com.thestar.member.security.PendingGoogleMember;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MemberGoogleAuthService {

    public static final String PENDING_GOOGLE_MEMBER_KEY = "pendingGoogleMember";
    public static final String MEMBER_LOGIN_REDIRECT_KEY = "memberLoginRedirect";
    private static final long PENDING_EXPIRE_MINUTES = 10L;

    private final MemberRepository memberRepository;
    private final MemberAuthService memberAuthService;
    private final MemberCouponService memberCouponService;
    private final PasswordEncoder passwordEncoder;

    public MemberGoogleAuthService(MemberRepository memberRepository,
                                   MemberAuthService memberAuthService,
                                   MemberCouponService memberCouponService,
                                   PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.memberAuthService = memberAuthService;
        this.memberCouponService = memberCouponService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public MemberVO prepareGoogleLogin(OidcUser oidcUser, HttpSession session) {
        String googleSub = trim(oidcUser.getSubject());
        String email = trim(oidcUser.getEmail()).toLowerCase();
        String name = trim(oidcUser.getFullName());
        Object verifiedValue = oidcUser.getClaims().get("email_verified");
        boolean emailVerified = Boolean.TRUE.equals(verifiedValue)
                || "true".equalsIgnoreCase(String.valueOf(verifiedValue));

        if (googleSub.isEmpty() || email.isEmpty() || !emailVerified) {
            throw new IllegalStateException("Google 帳號沒有可用的已驗證信箱");
        }

        MemberVO linkedMember = memberRepository.findByGoogleSub(googleSub).orElse(null);
        if (linkedMember != null) {
            memberAuthService.validateMemberCanLogin(linkedMember);
            session.removeAttribute(PENDING_GOOGLE_MEMBER_KEY);
            return linkedMember;
        }

        MemberVO sameEmailMember = memberRepository.findByMemberEmailIgnoreCase(email).orElse(null);
        if (sameEmailMember != null) {
            if (sameEmailMember.getGoogleSub() != null
                    && !sameEmailMember.getGoogleSub().equals(googleSub)) {
                throw new IllegalStateException("此會員信箱已綁定其他 Google 帳號");
            }

            PendingGoogleMember pending = new PendingGoogleMember(
                    googleSub,
                    email,
                    name.isEmpty() ? sameEmailMember.getMemberName() : name,
                    sameEmailMember.getMemberId(),
                    PendingGoogleMember.Mode.LINK_EXISTING
            );
            session.setAttribute(PENDING_GOOGLE_MEMBER_KEY, pending);
            return null;
        }

        PendingGoogleMember pending = new PendingGoogleMember(
                googleSub,
                email,
                name.isEmpty() ? "Google 會員" : name,
                null,
                PendingGoogleMember.Mode.NEW_MEMBER
        );
        session.setAttribute(PENDING_GOOGLE_MEMBER_KEY, pending);
        return null;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPendingProfile(HttpSession session) {
        PendingGoogleMember pending = getValidPending(session);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pending", true);
        result.put("mode", pending.getMode().name());
        result.put("memberEmail", pending.getEmail());
        result.put("memberName", pending.getName());
        return result;
    }

    @Transactional
    public MemberVO completeGoogleProfile(GoogleCompleteProfileRequest request, HttpSession session) {
        PendingGoogleMember pending = getValidPending(session);

        if (pending.getMode() == PendingGoogleMember.Mode.LINK_EXISTING) {
            MemberVO member = memberRepository.findById(pending.getExistingMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("原會員帳號不存在"));

            if (member.getMemberStatus() != null && member.getMemberStatus() == 2) {
                throw new IllegalStateException("帳號已停用，請聯絡客服");
            }

            String existingPassword = request.getExistingPassword() == null
                    ? ""
                    : request.getExistingPassword();
            if (member.getMemberPassword() == null
                    || existingPassword.isBlank()
                    || !passwordEncoder.matches(existingPassword, member.getMemberPassword())) {
                throw new IllegalArgumentException("原會員密碼錯誤，無法綁定 Google 帳號");
            }

            member.setGoogleSub(pending.getGoogleSub());
            if (member.getMemberStatus() == null || member.getMemberStatus() == 0) {
                member.setMemberStatus((byte) 1);
                member.setVerifyToken(null);
                member.setVerifyExpireTime(null);
            }

            MemberVO saved = memberRepository.save(member);
            session.removeAttribute(PENDING_GOOGLE_MEMBER_KEY);
            return saved;
        }

        String memberName = trim(request.getMemberName());
        String memberPhone = trim(request.getMemberPhone());
        String memberAddress = trim(request.getMemberAddress());
        memberAuthService.validateGoogleProfile(
                memberName,
                memberPhone,
                memberAddress,
                request.getMemberBirthday(),
                request.getMemberGender()
        );

        if (memberRepository.existsByMemberEmailIgnoreCase(pending.getEmail())) {
            throw new IllegalArgumentException("此信箱已存在，請重新使用 Google 登入並完成帳號綁定");
        }

        MemberVO member = new MemberVO();
        member.setMemberName(memberName);
        member.setMemberEmail(pending.getEmail());
        member.setMemberPassword(null);
        member.setMemberPhone(memberPhone);
        member.setMemberAddress(memberAddress);
        member.setMemberBirthday(request.getMemberBirthday());
        member.setMemberGender(request.getMemberGender());
        member.setMemberStatus((byte) 1);
        member.setGoogleSub(pending.getGoogleSub());

        MemberVO saved = memberRepository.save(member);
        memberCouponService.issueNewMemberCoupon(saved.getMemberId());
        memberCouponService.issueBirthdayCouponForMember(saved.getMemberId());
        session.removeAttribute(PENDING_GOOGLE_MEMBER_KEY);
        return saved;
    }

    private PendingGoogleMember getValidPending(HttpSession session) {
        Object value = session.getAttribute(PENDING_GOOGLE_MEMBER_KEY);
        if (!(value instanceof PendingGoogleMember pending)) {
            throw new IllegalArgumentException("Google 登入資料不存在，請重新登入");
        }

        if (pending.getCreatedAt().plusMinutes(PENDING_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            session.removeAttribute(PENDING_GOOGLE_MEMBER_KEY);
            throw new IllegalArgumentException("Google 登入資料已過期，請重新登入");
        }

        return pending;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
