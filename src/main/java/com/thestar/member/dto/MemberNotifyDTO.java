package com.thestar.member.dto;

import java.time.LocalDateTime;

public class MemberNotifyDTO {

    private Integer memberNotifyId;
    private String content;
    private Byte isRead;
    private LocalDateTime createdTime;

    public MemberNotifyDTO() {
    }

    public Integer getMemberNotifyId() {
        return memberNotifyId;
    }

    public void setMemberNotifyId(Integer memberNotifyId) {
        this.memberNotifyId = memberNotifyId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Byte getIsRead() {
        return isRead;
    }

    public void setIsRead(Byte isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}