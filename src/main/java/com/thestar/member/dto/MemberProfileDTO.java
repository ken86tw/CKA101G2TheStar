package com.thestar.member.dto;

import com.thestar.member.entity.MemberVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MemberProfileDTO {

    private Integer memberId;
    private String memberNumber;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private String memberAddress;
    private LocalDate memberBirthday;
    private Byte memberGender;
    private Byte memberStatus;
    private boolean hasPassword;

    public static MemberProfileDTO from(MemberVO member) {
        MemberProfileDTO dto = new MemberProfileDTO();
        dto.memberId = member.getMemberId();
        dto.memberNumber = buildMemberNumber(member);
        dto.memberName = member.getMemberName();
        dto.memberEmail = member.getMemberEmail();
        dto.memberPhone = member.getMemberPhone();
        dto.memberAddress = member.getMemberAddress();
        dto.memberBirthday = member.getMemberBirthday();
        dto.memberGender = member.getMemberGender();
        dto.memberStatus = member.getMemberStatus();
        dto.hasPassword = member.getMemberPassword() != null && !member.getMemberPassword().isBlank();
        return dto;
    }

    private static String buildMemberNumber(MemberVO member) {
        if (member.getMemberId() == null || member.getCreatedTime() == null) {
            return null;
        }

        String registerDate = member.getCreatedTime()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String serialNumber = String.format("%07d", member.getMemberId());

        return "M" + registerDate + serialNumber;
    }
    
    public Integer getMemberId() {
        return memberId;
    }

    public String getMemberNumber() {
        return memberNumber;
    }
    
    public String getMemberName() {
        return memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public LocalDate getMemberBirthday() {
        return memberBirthday;
    }

    public Byte getMemberGender() {
        return memberGender;
    }

    public Byte getMemberStatus() {
        return memberStatus;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }
}