package com.thestar.member.security;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 將既有會員明碼密碼一次轉成 BCrypt。
 * 已是 BCrypt 或 Google 帳號的 NULL 密碼不會重複處理。
 */
@Component
public class MemberPasswordMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MemberPasswordMigrationRunner.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberPasswordMigrationRunner(MemberRepository memberRepository,
                                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<MemberVO> members = memberRepository.findAll();
        int migratedCount = 0;

        for (MemberVO member : members) {
            String storedPassword = member.getMemberPassword();
            if (storedPassword == null || storedPassword.isBlank() || isBcrypt(storedPassword)) {
                continue;
            }

            member.setMemberPassword(passwordEncoder.encode(storedPassword));
            migratedCount++;
        }

        if (migratedCount > 0) {
            memberRepository.saveAll(members);
            log.info("已將 {} 筆會員明碼密碼轉為 BCrypt", migratedCount);
        }
    }

    private boolean isBcrypt(String value) {
        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }
}
