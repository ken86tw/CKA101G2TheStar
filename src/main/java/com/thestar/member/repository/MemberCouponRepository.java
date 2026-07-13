package com.thestar.member.repository;

import com.thestar.member.entity.MemberCouponVO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MemberCouponRepository
        extends JpaRepository<MemberCouponVO, Integer> {

    @EntityGraph(attributePaths = "coupon")
    List<MemberCouponVO>
    findByMemberIdOrderByClaimedTimeDesc(
            Integer memberId
    );

    boolean existsByMemberIdAndCoupon_CouponIdAndIssuePeriod(
            Integer memberId,
            Integer couponId,
            String issuePeriod
    );
    
    @EntityGraph(attributePaths = "coupon")
    List<MemberCouponVO>
    findByUsedStatusAndUsageEndTimeBetween(
            Byte usedStatus,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}