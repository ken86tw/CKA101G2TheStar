package com.thestar.member.controller;

import com.thestar.member.dto.MemberNotifyDTO;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberNotifyService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/notifications")
public class MemberNotifyController {

    private final MemberNotifyService memberNotifyService;

    public MemberNotifyController(
            MemberNotifyService memberNotifyService
    ) {
        this.memberNotifyService =
                memberNotifyService;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            HttpSession session
    ) {
        MemberVO loginMember =
                getLoginMember(session);

        if (loginMember == null) {
            return unauthorized();
        }

        Integer memberId =
                loginMember.getMemberId();

        List<MemberNotifyDTO> notifications =
                memberNotifyService
                        .getNotifications(memberId);

        long unreadCount =
                memberNotifyService
                        .getUnreadCount(memberId);

        return ResponseEntity.ok(
                Map.of(
                        "notifications",
                        notifications,
                        "unreadCount",
                        unreadCount
                )
        );
    }

    @PostMapping("/{memberNotifyId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Integer memberNotifyId,
            HttpSession session
    ) {
        MemberVO loginMember =
                getLoginMember(session);

        if (loginMember == null) {
            return unauthorized();
        }

        boolean success =
                memberNotifyService.markAsRead(
                        loginMember.getMemberId(),
                        memberNotifyId
                );

        if (!success) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "error",
                                    "找不到指定的通知"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of("success", true)
        );
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            HttpSession session
    ) {
        MemberVO loginMember =
                getLoginMember(session);

        if (loginMember == null) {
            return unauthorized();
        }

        int updatedCount =
                memberNotifyService.markAllAsRead(
                        loginMember.getMemberId()
                );

        return ResponseEntity.ok(
                Map.of(
                        "success",
                        true,
                        "updatedCount",
                        updatedCount
                )
        );
    }

    private MemberVO getLoginMember(
            HttpSession session
    ) {
        MemberVO loginMember =
                (MemberVO) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null
                || loginMember.getMemberId() == null) {
            return null;
        }

        return loginMember;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        Map.of(
                                "error",
                                "請先登入會員"
                        )
                );
    }
}