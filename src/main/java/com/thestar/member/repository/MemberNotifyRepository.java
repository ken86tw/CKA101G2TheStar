package com.thestar.member.repository;

import com.thestar.member.entity.MemberNotifyVO;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberNotifyRepository
        extends JpaRepository<MemberNotifyVO, Integer> {

    List<MemberNotifyVO>
    findByMemberIdOrderByCreatedTimeDesc(
            Integer memberId
    );

    long countByMemberIdAndIsRead(
            Integer memberId,
            Byte isRead
    );

    Optional<MemberNotifyVO>
    findByMemberNotifyIdAndMemberId(
            Integer memberNotifyId,
            Integer memberId
    );

    boolean existsByMemberIdAndContent(
            Integer memberId,
            String content
    );
}