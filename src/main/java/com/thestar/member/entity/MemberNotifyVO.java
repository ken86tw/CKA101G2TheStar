package com.thestar.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_NOTIFY")
public class MemberNotifyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_NOTIFY_ID")
    private Integer memberNotifyId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Integer memberId;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;

    @Column(name = "IS_READ", nullable = false)
    private Byte isRead = 0;

    @Column(
        name = "CREATED_TIME",
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdTime;

    public MemberNotifyVO() {
    }

    public Integer getMemberNotifyId() {
        return memberNotifyId;
    }

    public void setMemberNotifyId(Integer memberNotifyId) {
        this.memberNotifyId = memberNotifyId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
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
}