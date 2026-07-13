package com.thestar.member.controller;

import com.thestar.member.dto.MemberCouponDTO;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberCouponService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/coupons")
public class MemberCouponController {

    private final MemberCouponService memberCouponService;

    public MemberCouponController(
            MemberCouponService memberCouponService
    ) {
        this.memberCouponService = memberCouponService;
    }

    @GetMapping
    public ResponseEntity<?> getMyCoupons(
            HttpSession session
    ) {
        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null
                || loginMember.getMemberId() == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                        Map.of(
                            "error",
                            "請先登入會員"
                        )
                    );
        }

        List<MemberCouponDTO> coupons =
                memberCouponService.getMemberCoupons(
                        loginMember.getMemberId()
                );

        return ResponseEntity.ok(coupons);
    }
}