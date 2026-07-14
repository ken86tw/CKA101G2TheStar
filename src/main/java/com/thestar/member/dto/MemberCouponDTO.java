package com.thestar.member.dto;

import java.time.LocalDateTime;

public class MemberCouponDTO {

    private Integer memberCouponId;

    private String couponCode;

    private String couponName;

    private String description;

    private Byte discountType;

    private Integer discountAmount;

    private Integer discountPercent;

    private String issuePeriod;

    private LocalDateTime claimedTime;

    private LocalDateTime usageStartTime;

    private LocalDateTime usageEndTime;

    private LocalDateTime usedTime;

    private String displayStatus;

    public MemberCouponDTO() {
    }

    public Integer getMemberCouponId() {
        return memberCouponId;
    }

    public void setMemberCouponId(Integer memberCouponId) {
        this.memberCouponId = memberCouponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Byte getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Byte discountType) {
        this.discountType = discountType;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getIssuePeriod() {
        return issuePeriod;
    }

    public void setIssuePeriod(String issuePeriod) {
        this.issuePeriod = issuePeriod;
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

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }
}