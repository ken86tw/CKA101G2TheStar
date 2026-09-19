package com.thestar.restaurant.entity;

import java.io.Serializable;
import java.sql.Time;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "BUSINESS_HOURS")
public class BusinessHoursVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sessionId;
    private Time startTime;
    private Time endTime;
    
    // === 新增：上架/下架狀態屬性 ===
    private Boolean isAvailable; 

    @Id
    @Column(name = "SESSION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getSessionId() {
        return sessionId;
    }
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    @Column(name = "START_TIME")
    @NotNull(message = "開始時間：請勿空白")
    public Time getStartTime() {
        return startTime;
    }
    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    @Column(name = "END_TIME")
    @NotNull(message = "結束時間：請勿空白")
    public Time getEndTime() {
        return endTime;
    }
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    /*
     * =========================================================================
     * 新增：上架狀態控制 (TRUE = 上架中 / FALSE = 已下架)
     * =========================================================================
     */
    @Column(name = "IS_AVAILABLE")
    @NotNull(message = "上架狀態：請勿空白")
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}