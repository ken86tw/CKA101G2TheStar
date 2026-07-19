package com.thestar.member.security;

import com.thestar.member.entity.MemberVO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 使用的會員登入資料。
 * 保留會員識別與驗證需要的欄位，舊功能仍繼續使用 HttpSession 的 loginMember。
 */
public class MemberUserDetails implements UserDetails {

    private final Integer memberId;
    private final String memberName;
    private final String memberEmail;
    private final String memberPassword;
    private final Byte memberStatus;

    public MemberUserDetails(MemberVO member) {
        this.memberId = member.getMemberId();
        this.memberName = member.getMemberName();
        this.memberEmail = member.getMemberEmail();
        this.memberPassword = member.getMemberPassword();
        this.memberStatus = member.getMemberStatus();
    }

    public Integer getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public Byte getMemberStatus() {
        return memberStatus;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }

    @Override
    public String getPassword() {
        return memberPassword;
    }

    @Override
    public String getUsername() {
        return memberEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 帳密正確後才由 Controller 判斷 0 未驗證、2 停用，才能保留原本的明確提示。
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
