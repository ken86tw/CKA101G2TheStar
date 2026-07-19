package com.thestar.member.security;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PendingGoogleMember implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Mode {
        NEW_MEMBER,
        LINK_EXISTING
    }

    private final String googleSub;
    private final String email;
    private final String name;
    private final Integer existingMemberId;
    private final Mode mode;
    private final LocalDateTime createdAt;

    public PendingGoogleMember(String googleSub,
                               String email,
                               String name,
                               Integer existingMemberId,
                               Mode mode) {
        this.googleSub = googleSub;
        this.email = email;
        this.name = name;
        this.existingMemberId = existingMemberId;
        this.mode = mode;
        this.createdAt = LocalDateTime.now();
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Integer getExistingMemberId() {
        return existingMemberId;
    }

    public Mode getMode() {
        return mode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
