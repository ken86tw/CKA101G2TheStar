package com.thestar.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_COUPON")
public class MemberCouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_COUPON_ID")
    private Integer memberCouponId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Integer memberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUPON_ID", nullable = false)
    private CouponVO coupon;

    @Column(name = "ISSUE_PERIOD", nullable = false, length = 50)
    private String issuePeriod;

    @Column(name = "USED_STATUS", nullable = false)
    private Byte usedStatus = 0;

    @Column(name = "CLAIMED_TIME", nullable = false)
    private LocalDateTime claimedTime;

    @Column(name = "USAGE_START_TIME", nullable = false)
    private LocalDateTime usageStartTime;

    @Column(name = "USAGE_END_TIME", nullable = false)
    private LocalDateTime usageEndTime;

    @Column(name = "USED_TIME")
    private LocalDateTime usedTime;

    public MemberCouponVO() {
    }

    public Integer getMemberCouponId() {
        return memberCouponId;
    }

    public void setMemberCouponId(Integer memberCouponId) {
        this.memberCouponId = memberCouponId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public CouponVO getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponVO coupon) {
        this.coupon = coupon;
    }

    public String getIssuePeriod() {
        return issuePeriod;
    }

    public void setIssuePeriod(String issuePeriod) {
        this.issuePeriod = issuePeriod;
    }

    public Byte getUsedStatus() {
        return usedStatus;
    }

    public void setUsedStatus(Byte usedStatus) {
        this.usedStatus = usedStatus;
    }

    public LocalDateTime getClaimedTime() {
        return claimedTime;
    }

    public void setClaimedTime(LocalDateTime claimedTime) {
        this.claimedTime = claimedTime;
    }

    public LocalDateTime getUsageStartTime() {
        return usageStartTime;
    }

    public void setUsageStartTime(LocalDateTime usageStartTime) {
        this.usageStartTime = usageStartTime;
    }

    public LocalDateTime getUsageEndTime() {
        return usageEndTime;
    }

    public void setUsageEndTime(LocalDateTime usageEndTime) {
        this.usageEndTime = usageEndTime;
    }

    public LocalDateTime getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(LocalDateTime usedTime) {
        this.usedTime = usedTime;
    }
}