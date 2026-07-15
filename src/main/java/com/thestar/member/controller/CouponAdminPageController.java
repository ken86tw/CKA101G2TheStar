package com.thestar.member.controller;

import com.thestar.employee.security.EmployeeUserDetails;
import com.thestar.member.service.MemberCouponService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thestar/admin/coupon")
public class CouponAdminPageController {

    private final MemberCouponService memberCouponService;

    public CouponAdminPageController(
            MemberCouponService memberCouponService
    ) {
        this.memberCouponService =
                memberCouponService;
    }

    @GetMapping("/list")
    public String list(
            Model model,
            @AuthenticationPrincipal
            EmployeeUserDetails principal
    ) {
        model.addAttribute(
                "coupons",
                memberCouponService.getAllCoupons()
        );

        model.addAttribute(
                "currentEmployeeName",
                principal.getEmployee()
                        .getEmployeeName()
        );

        model.addAttribute(
                "isSuperAdmin",
                principal.getAuthorities().stream().anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_SUPER_ADMIN")
                        )
        );

        return "admin/coupon/list";
    }

    @PostMapping("/{couponId}/toggle-status")
    public String toggleStatus(
            @PathVariable Integer couponId,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberCouponService.updateCouponIssueStatus(couponId, enabled);

            redirectAttributes.addFlashAttribute(
                    "message", enabled ? "優惠券已啟用發放" : "優惠券已暫停發放"
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "error", exception.getMessage()
            );
        }

        return "redirect:/thestar/admin/coupon/list";
    }

    @PostMapping("/birthday/issue-current-month")
    public String issueCurrentMonthBirthdayCoupons(
            RedirectAttributes redirectAttributes
    ) {
        try {
            int issuedCount =
                    memberCouponService
                            .issueCurrentMonthBirthdayCoupons();

            redirectAttributes.addFlashAttribute("message", "本月壽星優惠券發放完成，共發放 " + issuedCount + " 張");

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/thestar/admin/coupon/list";
    }
}