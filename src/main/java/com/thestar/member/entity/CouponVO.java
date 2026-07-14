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
@Table(name = "COUPONS")
public class CouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_ID")
    private Integer couponId;

    @Column(name = "COUPON_CODE", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Column(name = "COUPON_NAME", nullable = false, length = 100)
    private String couponName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "DISCOUNT_TYPE", nullable = false)
    private Byte discountType;

    @Column(name = "DISCOUNT_AMOUNT")
    private Integer discountAmount;

    @Column(name = "DISCOUNT_PERCENT")
    private Integer discountPercent;

    @Column(name = "REMAINING_QUANTITY")
    private Integer remainingQuantity;

    @Column(name = "DEFAULT_VALID_DAYS")
    private Integer defaultValidDays;

    @Column(name = "ISSUE_STATUS", nullable = false)
    private Byte issueStatus = 0;

    @Column(
        name = "CREATED_TIME",
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdTime;

    @Column(
        name = "UPDATED_TIME",
        insertable = false,
        updatable = false
    )
    private LocalDateTime updatedTime;

    public CouponVO() {
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
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

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public Integer getDefaultValidDays() {
        return defaultValidDays;
    }

    public void setDefaultValidDays(Integer defaultValidDays) {
        this.defaultValidDays = defaultValidDays;
    }

    public Byte getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(Byte issueStatus) {
        this.issueStatus = issueStatus;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
}