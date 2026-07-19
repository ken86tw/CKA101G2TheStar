package com.thestar.member.service;

import com.thestar.member.dto.MemberProfileUpdateRequest;
import com.thestar.member.dto.MemberRegisterRequest;
import com.thestar.member.dto.MemberRegisterResponse;
import com.thestar.member.dto.MemberVerifyResponse;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MemberAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern TAIWAN_PHONE_PATTERN = Pattern.compile("^09\\d{8}$");
    private static final long MAX_PICTURE_SIZE = 5L * 1024L * 1024L;
    private static final long VERIFY_EXPIRE_MINUTES = 10L;

    private final MemberRepository memberRepository;
    private final MemberVerificationMailService verificationMailService;
    private final MemberCouponService memberCouponService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.member.password-reset-expire-minutes:30}")
    private long passwordResetExpireMinutes;

    public MemberAuthService(MemberRepository memberRepository,
                             MemberVerificationMailService verificationMailService,
                             MemberCouponService memberCouponService,
                             PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.verificationMailService = verificationMailService;
        this.memberCouponService = memberCouponService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberRegisterResponse register(MemberRegisterRequest request) throws IOException {
        String memberName = trim(request.getMemberName());
        String memberEmail = trim(request.getMemberEmail()).toLowerCase();
        String memberPassword = request.getMemberPassword() == null ? "" : request.getMemberPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();
        String memberPhone = trim(request.getMemberPhone());
        String memberAddress = trim(request.getMemberAddress());
        Byte memberGender = request.getMemberGender() == null ? 2 : request.getMemberGender();
        MultipartFile memberPictureFile = request.getMemberPicture();

        // 這段保留你原本 Servlet insert() 的判斷：必填、手機格式、性別、圖片大小。
        validateRegisterData(
                memberName,
                memberEmail,
                memberPassword,
                confirmPassword,
                memberPhone,
                memberAddress,
                request.getMemberBirthday(),
                memberGender,
                memberPictureFile
        );

        if (memberRepository.existsByMemberEmailIgnoreCase(memberEmail)) {
            throw new IllegalArgumentException("新增失敗，可能是信箱重複");
        }

        MemberVO member = new MemberVO();
        member.setMemberName(memberName);
        member.setMemberEmail(memberEmail);
        member.setMemberPassword(passwordEncoder.encode(memberPassword));
        member.setMemberPhone(memberPhone);
        member.setMemberAddress(memberAddress);
        member.setMemberBirthday(request.getMemberBirthday());
        member.setMemberGender(memberGender);
        member.setMemberStatus((byte) 0);

        if (memberPictureFile != null && !memberPictureFile.isEmpty()) {
            member.setMemberPicture(memberPictureFile.getBytes());
        }

        String verifyToken = createToken();
        member.setVerifyToken(verifyToken);
        member.setVerifyExpireTime(LocalDateTime.now().plusMinutes(VERIFY_EXPIRE_MINUTES));

        MemberVO savedMember = memberRepository.save(member);

        String verifyUrl = verificationMailService.buildVerifyUrl(savedMember.getVerifyToken());
        System.out.println("會員驗證連結：");
        System.out.println(verifyUrl);

        boolean mailSent = verificationMailService.sendVerifyMail(savedMember, verifyUrl);
        String message = mailSent
                ? "註冊成功，請到信箱收取驗證信"
                : "註冊成功，但驗證信寄送失敗，請先使用畫面上的開發測試驗證連結";

        return new MemberRegisterResponse(true, message, mailSent, verifyUrl);
    }

    @Transactional
    public MemberVerifyResponse verifyEmail(String token) {
        String cleanToken = trim(token);

        if (cleanToken.isEmpty()) {
            throw new IllegalArgumentException("驗證連結無效");
        }

        MemberVO member = memberRepository.findByVerifyToken(cleanToken)
                .orElseThrow(() -> new IllegalArgumentException("驗證連結不存在，或帳號已經驗證過"));

        if (member.getVerifyExpireTime() != null
                && member.getVerifyExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("驗證連結已過期，請重新寄送驗證信");
        }

        member.setMemberStatus((byte) 1);
        member.setVerifyToken(null);
        member.setVerifyExpireTime(null);

        MemberVO savedMember = memberRepository.save(member);
        boolean newMemberCouponIssued = memberCouponService.issueNewMemberCoupon(savedMember.getMemberId());
        boolean birthdayCouponIssued = memberCouponService.issueBirthdayCouponForMember(savedMember.getMemberId());
        StringBuilder message = new StringBuilder("會員信箱驗證成功，帳號已啟用");

        if (newMemberCouponIssued) {
            message.append("，新會員優惠券已發放至您的帳戶");
        }
        if (birthdayCouponIssued) {
            message.append("，本月壽星優惠券也已發放至您的帳戶");
        }

        return new MemberVerifyResponse(true, message.toString());
    }

    @Transactional
    public MemberRegisterResponse resendVerificationMail(String email) {
        String cleanEmail = trim(email).toLowerCase();

        if (cleanEmail.isEmpty()) {
            throw new IllegalArgumentException("請輸入會員信箱");
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("信箱格式錯誤");
        }

        MemberVO member = memberRepository.findByMemberEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("查無此會員信箱"));

        if (member.getMemberStatus() != null && member.getMemberStatus() == 1) {
            throw new IllegalArgumentException("此帳號已完成信箱驗證，請直接登入");
        }
        if (member.getMemberStatus() != null && member.getMemberStatus() == 2) {
            throw new IllegalArgumentException("帳號已停用，請聯絡客服");
        }

        String verifyToken = createToken();
        member.setVerifyToken(verifyToken);
        member.setVerifyExpireTime(LocalDateTime.now().plusMinutes(VERIFY_EXPIRE_MINUTES));

        MemberVO savedMember = memberRepository.save(member);
        String verifyUrl = verificationMailService.buildVerifyUrl(savedMember.getVerifyToken());
        System.out.println("重新寄送會員驗證連結：");
        System.out.println(verifyUrl);

        boolean mailSent = verificationMailService.sendVerifyMail(savedMember, verifyUrl);
        String message = mailSent ? "驗證信已重新寄出，請到信箱收信" : "驗證信寄送失敗，請稍後再試";

        return new MemberRegisterResponse(true, message, mailSent, verifyUrl);
    }

    @Transactional(readOnly = true)
    public MemberVO getMemberForAuthenticatedLogin(Integer memberId) {
        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("信箱或密碼錯誤"));

        validateMemberCanLogin(member);
        return member;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String cleanEmail = trim(email).toLowerCase();

        if (cleanEmail.isEmpty() || !EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return;
        }

        MemberVO member = memberRepository.findByMemberEmailIgnoreCase(cleanEmail).orElse(null);
        if (member == null || (member.getMemberStatus() != null && member.getMemberStatus() == 2)) {
            return;
        }

        member.setResetToken(createToken());
        member.setResetExpireTime(LocalDateTime.now().plusMinutes(passwordResetExpireMinutes));
        memberRepository.save(member);

        String resetUrl = verificationMailService.buildResetPasswordUrl(member.getResetToken());
        System.out.println("會員密碼重設連結：");
        System.out.println(resetUrl);
        verificationMailService.sendPasswordResetMail(member, resetUrl, passwordResetExpireMinutes);
    }

    @Transactional(readOnly = true)
    public boolean isResetTokenValid(String token) {
        String cleanToken = trim(token);
        if (cleanToken.isEmpty()) {
            return false;
        }

        return memberRepository.findByResetToken(cleanToken)
                .filter(member -> member.getResetExpireTime() != null)
                .filter(member -> member.getResetExpireTime().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        String cleanToken = trim(token);
        String password = newPassword == null ? "" : newPassword;
        String confirm = confirmPassword == null ? "" : confirmPassword;

        if (cleanToken.isEmpty()) {
            throw new IllegalArgumentException("密碼重設連結無效");
        }
        validateNewPassword(password, confirm);

        MemberVO member = memberRepository.findByResetToken(cleanToken)
                .orElseThrow(() -> new IllegalArgumentException("密碼重設連結不存在或已使用"));

        if (member.getResetExpireTime() == null
                || member.getResetExpireTime().isBefore(LocalDateTime.now())) {
            member.setResetToken(null);
            member.setResetExpireTime(null);
            memberRepository.save(member);
            throw new IllegalArgumentException("密碼重設連結已過期，請重新申請");
        }

        member.setMemberPassword(passwordEncoder.encode(password));
        member.setResetToken(null);
        member.setResetExpireTime(null);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberVO getMemberById(Integer memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));
    }

    @Transactional
    public MemberVO updateProfile(Integer memberId, MemberProfileUpdateRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }

        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));

        String name = trim(request.getMemberName());
        String phone = trim(request.getMemberPhone());
        String address = trim(request.getMemberAddress());

        if (name.isEmpty()) {
            throw new IllegalArgumentException("姓名不可空白");
        }
        if (!phone.isEmpty() && !TAIWAN_PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("手機格式錯誤，請輸入 09 開頭共 10 碼");
        }

        member.setMemberName(name);
        member.setMemberPhone(phone);
        member.setMemberAddress(address);
        member.setMemberGender(request.getMemberGender());

        return memberRepository.save(member);
    }

    @Transactional
    public MemberVO changePassword(Integer memberId,
                                   String currentPassword,
                                   String newPassword,
                                   String confirmPassword) {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }

        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));

        String storedPassword = member.getMemberPassword();
        String current = currentPassword == null ? "" : currentPassword;
        String password = newPassword == null ? "" : newPassword;
        String confirm = confirmPassword == null ? "" : confirmPassword;

        boolean hasPassword =
                storedPassword != null && !storedPassword.isBlank();

        // 一般信箱會員必須驗證目前密碼
        if (hasPassword) {
            if (current.isBlank()) {
                throw new IllegalArgumentException("請輸入目前密碼");
            }

            if (!passwordEncoder.matches(current, storedPassword)) {
                throw new IllegalArgumentException("目前密碼錯誤");
            }
        }

        validateNewPassword(password, confirm);

        if (hasPassword
                && passwordEncoder.matches(password, storedPassword)) {
            throw new IllegalArgumentException("新密碼不可與目前密碼相同");
        }

        member.setMemberPassword(passwordEncoder.encode(password));

        // 修改密碼後，清除先前可能存在的忘記密碼連結
        member.setResetToken(null);
        member.setResetExpireTime(null);

        return memberRepository.save(member);
    }
    
    @Transactional
    public MemberVO updateProfilePicture(Integer memberId, MultipartFile picture) throws IOException {
        if (memberId == null) {
            throw new IllegalArgumentException("請先登入會員");
        }
        if (picture == null || picture.isEmpty()) {
            throw new IllegalArgumentException("請選擇要上傳的照片");
        }

        validatePicture(picture);
        MemberVO member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));
        member.setMemberPicture(picture.getBytes());
        return memberRepository.save(member);
    }

    public void validateGoogleProfile(String name,
                                      String phone,
                                      String address,
                                      LocalDate birthday,
                                      Byte gender) {
        if (trim(name).isEmpty()) {
            throw new IllegalArgumentException("會員姓名請勿空白");
        }
        if (trim(phone).isEmpty() || !TAIWAN_PHONE_PATTERN.matcher(trim(phone)).matches()) {
            throw new IllegalArgumentException("手機格式錯誤，請輸入 09 開頭的 10 碼手機號碼");
        }
        if (trim(address).isEmpty()) {
            throw new IllegalArgumentException("會員地址請勿空白");
        }
        if (birthday == null) {
            throw new IllegalArgumentException("會員生日請勿空白");
        }
        if (!isValidChoice(gender)) {
            throw new IllegalArgumentException("會員性別格式不正確");
        }
    }

    public void validateMemberCanLogin(MemberVO member) {
        if (member.getMemberStatus() == null || member.getMemberStatus() == 0) {
            throw new IllegalStateException("帳號尚未完成信箱驗證");
        }
        if (member.getMemberStatus() == 2) {
            throw new IllegalStateException("帳號已停用，請聯絡客服");
        }
    }

    private void validateRegisterData(String memberName,
                                      String memberEmail,
                                      String memberPassword,
                                      String confirmPassword,
                                      String memberPhone,
                                      String memberAddress,
                                      LocalDate memberBirthday,
                                      Byte memberGender,
                                      MultipartFile memberPicture) {
        if (memberName.isEmpty()) {
            throw new IllegalArgumentException("會員姓名請勿空白");
        }
        if (memberEmail.isEmpty()) {
            throw new IllegalArgumentException("會員信箱請勿空白");
        }
        if (!EMAIL_PATTERN.matcher(memberEmail).matches()) {
            throw new IllegalArgumentException("會員信箱格式不正確");
        }
        // confirmPassword 是 Vue 版多加的前端保護，不會改掉你原本密碼必填判斷。
        validateNewPassword(memberPassword, confirmPassword);
        if (memberPhone.isEmpty()) {
            throw new IllegalArgumentException("會員手機請勿空白");
        }
        if (!TAIWAN_PHONE_PATTERN.matcher(memberPhone).matches()) {
            throw new IllegalArgumentException("手機格式錯誤，請輸入 09 開頭的 10 碼手機號碼");
        }
        if (memberAddress.isEmpty()) {
            throw new IllegalArgumentException("會員地址請勿空白");
        }
        if (memberBirthday == null) {
            throw new IllegalArgumentException("會員生日請勿空白");
        }
        if (!isValidChoice(memberGender)) {
            throw new IllegalArgumentException("會員性別格式不正確");
        }
        if (memberPicture != null && !memberPicture.isEmpty()) {
            validatePicture(memberPicture);
        }
    }

    private void validateNewPassword(String password, String confirmPassword) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("會員密碼請勿空白");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("確認密碼請勿空白");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("兩次輸入的密碼不一致");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("會員密碼至少需要 6 個字元");
        }
    }

    private void validatePicture(MultipartFile picture) {
        if (picture.getSize() > MAX_PICTURE_SIZE) {
            throw new IllegalArgumentException("圖片大小不可超過 5MB");
        }

        Set<String> allowTypes = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
        String contentType = picture.getContentType();
        if (contentType != null && !contentType.isBlank() && !allowTypes.contains(contentType)) {
            throw new IllegalArgumentException("照片格式只支援 JPG、PNG、GIF、WEBP");
        }
    }

    private String createToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isValidChoice(Byte value) {
        return value != null && (value == 0 || value == 1 || value == 2);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
