package com.thestar.member.security;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

/**
 * 會員 API 專用 SecurityFilterChain。
 * 訂房、購物、餐廳等舊功能仍可繼續讀取 loginMember，不需要跟著重寫。
 */
@Configuration
public class MemberSecurityConfig {

    @Bean("memberAuthenticationManager")
    public AuthenticationManager memberAuthenticationManager(MemberRepository memberRepository,
                                                               PasswordEncoder passwordEncoder) {
        UserDetailsService memberUserDetailsService = memberEmail -> {
            MemberVO member = memberRepository.findByMemberEmailIgnoreCase(memberEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("信箱或密碼錯誤"));
            return new MemberUserDetails(member);
        };

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(memberUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @Order(3)
    public SecurityFilterChain memberApiFilterChain(
            HttpSecurity http,
            @Qualifier("memberAuthenticationManager") AuthenticationManager memberAuthenticationManager,
            MemberSecurityContextSupport memberSecurityContextSupport) throws Exception {

        http
                .securityMatcher("/api/member/**")
                .authenticationManager(memberAuthenticationManager)
                .csrf(csrf -> csrf.disable())
                .sessionManagement((SessionManagementConfigurer<HttpSecurity> sm) ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(context -> context
                        .securityContextRepository(memberSecurityContextSupport.repository()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/member/register",
                                "/api/member/verify",
                                "/api/member/resend-verification",
                                "/api/member/login",
                                "/api/member/status",
                                "/api/member/logout",
                                "/api/member/forgot-password",
                                "/api/member/reset-password",
                                "/api/member/reset-password/**",
                                "/api/member/google/enabled",
                                "/api/member/google/pending",
                                "/api/member/google/complete"
                        ).permitAll()
                        .anyRequest().hasRole("MEMBER")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) ->
                                writeJson(response, 401, "請先登入會員"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeJson(response, 403, "此功能需要會員身分"))
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    private static void writeJson(jakarta.servlet.http.HttpServletResponse response,
                                  int status,
                                  String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
