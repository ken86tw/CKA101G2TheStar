package com.thestar.member.scheduler;

import com.thestar.member.service.MemberCouponService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CouponExpirationReminderScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CouponExpirationReminderScheduler.class
            );

    private final MemberCouponService
            memberCouponService;

    public CouponExpirationReminderScheduler(
            MemberCouponService memberCouponService
    ) {
        this.memberCouponService =
                memberCouponService;
    }

    /*
     * 每天早上 09:00 執行。
     */
    @Scheduled(
//            cron = "0 0 9 * * *",
            cron = "0 * * * * *",		//test
            zone = "Asia/Taipei"
    )
    public void sendExpirationReminders() {

        try {

            int notificationCount =
                    memberCouponService
                            .createExpiringCouponNotifications();

            logger.info(
                    "優惠券到期提醒完成，共新增 {} 則通知",
                    notificationCount
            );

        } catch (Exception exception) {

            logger.error(
                    "優惠券到期提醒執行失敗",
                    exception
            );
        }
    }
}