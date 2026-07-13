package com.thestar.member.scheduler;

import com.thestar.member.service.MemberCouponService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BirthdayCouponScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    BirthdayCouponScheduler.class
            );

    private final MemberCouponService memberCouponService;

    public BirthdayCouponScheduler(
            MemberCouponService memberCouponService
    ) {
        this.memberCouponService = memberCouponService;
    }
    /**
     * 每月 1 日凌晨 00:05 執行。
     *
     * cron 六個位置依序為：
     * 秒、分、時、日、月、星期
     */
    @Scheduled(
//            cron = "0 5 0 1 * *",
            cron = "0 * * * * *",			//test
            zone = "Asia/Taipei"
    )
    public void issueMonthlyBirthdayCoupons() {

        try {
            int issuedCount =memberCouponService.issueCurrentMonthBirthdayCoupons();
            logger.info("本月生日優惠券自動發放完成，共發放 {} 張",issuedCount);
        } catch (Exception exception) {
            logger.error("本月生日優惠券自動發放失敗",exception);
        }
    }
}