package com.thestar.member.service;

import com.thestar.member.dto.MemberNotifyDTO;
import com.thestar.member.entity.MemberNotifyVO;
import com.thestar.member.repository.MemberNotifyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberNotifyService {

    private static final byte NOT_READ = 0;
    private static final byte READ = 1;

    private final MemberNotifyRepository memberNotifyRepository;

    public MemberNotifyService(
            MemberNotifyRepository memberNotifyRepository
    ) {
        this.memberNotifyRepository =
                memberNotifyRepository;
    }

    @Transactional
    public MemberNotifyVO createNotification(
            Integer memberId,
            String content
    ) {
        if (memberId == null) {
            throw new IllegalArgumentException(
                    "會員編號不可為空"
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "通知內容不可為空"
            );
        }

        MemberNotifyVO notification =
                new MemberNotifyVO();

        notification.setMemberId(memberId);
        notification.setContent(content.trim());
        notification.setIsRead(NOT_READ);

        return memberNotifyRepository.save(
                notification
        );
    }
    
    @Transactional(readOnly = true)
    public boolean notificationExists(
            Integer memberId,
            String content
    ) {
        if (memberId == null
                || content == null
                || content.isBlank()) {

            return false;
        }

        return memberNotifyRepository
                .existsByMemberIdAndContent(
                        memberId,
                        content.trim()
                );
    }

    @Transactional(readOnly = true)
    public List<MemberNotifyDTO> getNotifications(
            Integer memberId
    ) {
        if (memberId == null) {
            throw new IllegalArgumentException(
                    "會員編號不可為空"
            );
        }

        return memberNotifyRepository
                .findByMemberIdOrderByCreatedTimeDesc(
                        memberId
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(
            Integer memberId
    ) {
        if (memberId == null) {
            return 0;
        }

        return memberNotifyRepository
                .countByMemberIdAndIsRead(
                        memberId,
                        NOT_READ
                );
    }

    @Transactional
    public boolean markAsRead(
            Integer memberId,
            Integer memberNotifyId
    ) {
        if (memberId == null
                || memberNotifyId == null) {
            return false;
        }

        MemberNotifyVO notification =
                memberNotifyRepository
                        .findByMemberNotifyIdAndMemberId(
                                memberNotifyId,
                                memberId
                        )
                        .orElse(null);

        if (notification == null) {
            return false;
        }

        if (notification.getIsRead() != null
                && notification.getIsRead() == READ) {
            return true;
        }

        notification.setIsRead(READ);

        memberNotifyRepository.save(notification);

        return true;
    }

    @Transactional
    public int markAllAsRead(
            Integer memberId
    ) {
        if (memberId == null) {
            return 0;
        }

        List<MemberNotifyVO> notifications =
                memberNotifyRepository
                        .findByMemberIdOrderByCreatedTimeDesc(
                                memberId
                        );

        int updatedCount = 0;

        for (MemberNotifyVO notification : notifications) {

            if (notification.getIsRead() == null
                    || notification.getIsRead() == NOT_READ) {

                notification.setIsRead(READ);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            memberNotifyRepository.saveAll(
                    notifications
            );
        }

        return updatedCount;
    }

    private MemberNotifyDTO toDTO(
            MemberNotifyVO notification
    ) {
        MemberNotifyDTO dto =
                new MemberNotifyDTO();

        dto.setMemberNotifyId(
                notification.getMemberNotifyId()
        );

        dto.setContent(
                notification.getContent()
        );

        dto.setIsRead(
                notification.getIsRead()
        );

        dto.setCreatedTime(
                notification.getCreatedTime()
        );

        return dto;
    }
}